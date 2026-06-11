<script setup>
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'

const props = defineProps({
  /** 当前纬度 (GCJ-02) */
  lat: { type: Number, default: 39.909187 },
  /** 当前经度 (GCJ-02) */
  lng: { type: Number, default: 116.397428 },
  /** 打卡半径（米） */
  radius: { type: Number, default: 500 },
  /** 是否禁用交互 */
  disabled: { type: Boolean, default: false },
})

const emit = defineEmits(['update:lat', 'update:lng'])

const mapContainer = ref(null)
const loading = ref(true)
const loadError = ref(null)

/** 统一坐标精度到小数点后 6 位 */
function roundCoord(val) {
  return val != null ? Math.round(val * 1e6) / 1e6 : val
}

// 搜索相关
const searchKeyword = ref('')
const searchTips = ref([])
const searchFocused = ref(false)
const searching = ref(false)
let searchTimer = null

let mapInstance = null
let markerInstance = null
let circleInstance = null
let geocoder = null
let amapKey = ''

onMounted(async () => {
  await nextTick()
  initMap()
})

onUnmounted(() => {
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
})

watch(() => [props.lat, props.lng], ([newLat, newLng]) => {
  if (mapInstance && newLat && newLng) {
    const pos = [newLng, newLat]
    mapInstance.setCenter(pos)
    updateMarker(pos)
  }
})

watch(() => props.radius, (newRadius) => {
  if (circleInstance && newRadius) {
    circleInstance.setRadius(newRadius)
  }
})

async function initMap() {
  try {
    amapKey = import.meta.env.VITE_AMAP_KEY || import.meta.env.VITE_AMAP_KEY_JS || ''
    if (!amapKey) {
      loadError.value = '未配置高德地图 API Key，请在 .env 文件中设置 VITE_AMAP_KEY'
      loading.value = false
      return
    }

    const AMap = await AMapLoader.load({
      key: amapKey,
      version: '2.0',
      plugins: ['AMap.Geocoder'],
    })

    geocoder = new AMap.Geocoder({})

    const center = [props.lng || 116.397428, props.lat || 39.909187]

    mapInstance = new AMap.Map(mapContainer.value, {
      center: center,
      zoom: 15,
      resizeEnable: true,
    })

    if (!props.disabled) {
      mapInstance.on('click', (e) => {
        const pos = [e.lnglat.getLng(), e.lnglat.getLat()]
        updateMarker(pos)
        emit('update:lng', roundCoord(e.lnglat.getLng()))
        emit('update:lat', roundCoord(e.lnglat.getLat()))
        reverseGeocode(e.lnglat.getLng(), e.lnglat.getLat())
      })
    }

    updateMarker(center)
    loading.value = false
  } catch (e) {
    console.error('高德地图加载失败:', e)
    loadError.value = '地图加载失败，请检查 API Key 是否正确'
    loading.value = false
  }
}

/** 搜索输入（防抖） */
function handleSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    searchTips.value = []
    return
  }
  searchTimer = setTimeout(() => {
    doSearch(keyword)
  }, 300)
}

/** 调用高德 POI 搜索 REST API */
async function doSearch(keyword) {
  if (!amapKey) return
  searching.value = true
  try {
    const url = `https://restapi.amap.com/v3/place/text?key=${amapKey}&keywords=${encodeURIComponent(keyword)}&city=全国&offset=10&output=JSON`
    const resp = await fetch(url)
    const json = await resp.json()
    if (json.status === '1' && json.pois) {
      searchTips.value = json.pois.map(poi => ({
        name: poi.name,
        address: poi.address || '',
        district: (poi.adname || '') + ' ',
        location: { lng: Number(poi.location.split(',')[0]), lat: Number(poi.location.split(',')[1]) },
      }))
    } else {
      searchTips.value = []
    }
  } catch (e) {
    console.error('POI 搜索失败:', e)
    searchTips.value = []
  } finally {
    searching.value = false
  }
}

/** 选中搜索建议 */
function handleSelectTip(tip) {
  searchTips.value = []
  searchFocused.value = false
  searchKeyword.value = tip.name + ' ' + (tip.address || '')
  if (tip.location) {
    const pos = [tip.location.lng, tip.location.lat]
    mapInstance.setZoomAndCenter(16, pos)
    updateMarker(pos)
    emit('update:lng', roundCoord(tip.location.lng))
    emit('update:lat', roundCoord(tip.location.lat))
  }
}

function handleFocus() {
  searchFocused.value = true
  if (searchKeyword.value.trim()) {
    doSearch(searchKeyword.value.trim())
  }
}

function handleBlur() {
  setTimeout(() => {
    searchFocused.value = false
    searchTips.value = []
  }, 200)
}

function clearSearch() {
  searchKeyword.value = ''
  searchTips.value = []
}

function reverseGeocode(lng, lat) {
  if (!geocoder) return
  geocoder.getAddress([lng, lat], (status, result) => {
    if (status === 'complete' && result.regeocode) {
      searchKeyword.value = result.regeocode.formattedAddress || ''
    }
  })
}

function updateMarker(center) {
  const AMap = window.AMap
  if (!AMap) return

  if (markerInstance) {
    markerInstance.setPosition(center)
  } else {
    markerInstance = new AMap.Marker({
      position: center,
      map: mapInstance,
      draggable: !props.disabled,
      title: '打卡中心点',
    })
    if (!props.disabled) {
      markerInstance.on('dragend', (e) => {
        const pos = [e.lnglat.getLng(), e.lnglat.getLat()]
        emit('update:lng', roundCoord(e.lnglat.getLng()))
        emit('update:lat', roundCoord(e.lnglat.getLat()))
        updateCircle(pos)
        reverseGeocode(e.lnglat.getLng(), e.lnglat.getLat())
      })
    }
  }

  updateCircle(center)
}

function updateCircle(center) {
  const AMap = window.AMap
  if (!AMap) return

  if (circleInstance) {
    circleInstance.setCenter(center)
    circleInstance.setRadius(props.radius || 500)
  } else {
    circleInstance = new AMap.Circle({
      center: center,
      radius: props.radius || 500,
      map: mapInstance,
      fillColor: '#1890ff',
      fillOpacity: 0.15,
      strokeColor: '#1890ff',
      strokeWeight: 2,
      strokeOpacity: 0.8,
    })
  }
}
</script>

<template>
  <div class="amap-picker">
    <div v-if="loadError" class="amap-error">
      <span class="error-icon">⚠️</span>
      <span class="error-text">{{ loadError }}</span>
      <span class="error-hint">获取 Key：<a href="https://console.amap.com/dev/key/app" target="_blank">高德开放平台</a></span>
    </div>
    <div v-if="loading && !loadError" class="amap-loading">
      <span>加载地图中...</span>
    </div>

    <!-- 搜索栏 -->
    <div v-if="!loading && !loadError" class="amap-search-bar">
      <div class="search-input-wrapper">
        <span class="search-icon">🔍</span>
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索地点，如：XX工地、XX大厦"
          @input="handleSearchInput"
          @focus="handleFocus"
          @blur="handleBlur"
        />
        <span v-if="searching" class="search-spinner">⏳</span>
        <span v-else-if="searchKeyword" class="search-clear" @mousedown.prevent="clearSearch">✕</span>
      </div>
      <!-- 搜索建议下拉 -->
      <ul v-if="searchTips.length > 0 && searchFocused" class="search-tips">
        <li
          v-for="(tip, idx) in searchTips"
          :key="idx"
          class="search-tip-item"
          @mousedown.prevent="handleSelectTip(tip)"
        >
          <span class="tip-name">{{ tip.name }}</span>
          <span class="tip-addr">{{ tip.district }}{{ tip.address }}</span>
        </li>
      </ul>
    </div>

    <div ref="mapContainer" class="amap-container" :class="{ 'amap-hidden': loading || loadError }"></div>
    <div class="amap-tip" v-if="!disabled && !loadError">
      💡 搜索或点击地图设置打卡中心点，拖动标记可微调位置，蓝色圆圈为打卡范围
    </div>
  </div>
</template>

<style scoped>
.amap-picker {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #d9d9d9;
}
.amap-container {
  width: 100%;
  height: 320px;
}
.amap-hidden {
  height: 0;
}
.amap-loading,
.amap-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #999;
  gap: 8px;
  font-size: 14px;
}
.amap-error .error-icon {
  font-size: 32px;
}
.amap-error .error-text {
  color: #ff4d4f;
}
.amap-error .error-hint {
  font-size: 12px;
  color: #999;
}

/* 搜索栏 */
.amap-search-bar {
  position: relative;
  background: #fff;
}
.search-input-wrapper {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  gap: 8px;
}
.search-icon {
  font-size: 14px;
  flex-shrink: 0;
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #333;
  background: transparent;
  padding: 4px 0;
}
.search-input::placeholder {
  color: #bfbfbf;
}
.search-spinner {
  font-size: 12px;
  flex-shrink: 0;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.search-clear {
  cursor: pointer;
  font-size: 12px;
  color: #999;
  padding: 2px 4px;
  flex-shrink: 0;
}
.search-clear:hover {
  color: #666;
}

/* 搜索建议 */
.search-tips {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-top: none;
  max-height: 240px;
  overflow-y: auto;
  list-style: none;
  margin: 0;
  padding: 0;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.search-tip-item {
  display: flex;
  flex-direction: column;
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
}
.search-tip-item:hover {
  background: #e6f7ff;
}
.search-tip-item:last-child {
  border-bottom: none;
}
.tip-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.tip-addr {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.amap-tip {
  padding: 8px 12px;
  font-size: 12px;
  color: #999;
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
}
</style>
