package cn.tx.aiagent.dict.api;

import cn.tx.aiagent.common.api.Result;
import cn.tx.aiagent.dict.infrastructure.DictMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DictController {

    private final DictMapper dictMapper;

    public DictController(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    @GetMapping("/dicts/{type}")
    public Result<List<DictResponse>> listByType(@PathVariable String type,
                                                  @RequestParam(required = false) Boolean enabled) {
        // 默认只返回启用的项
        Boolean filter = enabled != null ? enabled : true;
        List<DictResponse> list = dictMapper.selectByType(type, filter)
                .stream()
                .map(po -> {
                    DictResponse r = new DictResponse();
                    r.setCode(po.getCode());
                    r.setLabel(po.getLabel());
                    r.setSort(po.getSort());
                    return r;
                })
                .toList();
        return Result.success(list);
    }
}
