-- ============================================================
-- project-manage 数据库建表脚本（MySQL 8.0+ 版本）
-- 由 schema.sql (PostgreSQL) 转换而来
-- ============================================================

-- -----------------------------------------------------------
-- 辅助存储过程：安全添加列（模拟 ADD COLUMN IF NOT EXISTS）
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_add_column_if_not_exists;

DELIMITER //
CREATE PROCEDURE proc_add_column_if_not_exists(
    IN tbl_name  VARCHAR(64),
    IN col_name  VARCHAR(64),
    IN col_def   VARCHAR(200)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = tbl_name
          AND COLUMN_NAME = col_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', tbl_name, '` ADD COLUMN ', col_def);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- ============================================================
-- 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_user (
    id                   BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username             VARCHAR(50)  NOT NULL COMMENT '用户名',
    password_hash        VARCHAR(255) NOT NULL COMMENT '密码哈希',
    email                VARCHAR(100) COMMENT '邮箱',
    phone                VARCHAR(20)  COMMENT '手机号',
    status               SMALLINT     NOT NULL DEFAULT 1  COMMENT '1=正常, 0=禁用',
    must_change_password SMALLINT     NOT NULL DEFAULT 0  COMMENT '0=无需改密, 1=首次登录需改密',
    is_deleted           SMALLINT     NOT NULL DEFAULT 0  COMMENT '0=正常, 1=已删除',
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- DDL 增量变更：用户真实姓名
CALL proc_add_column_if_not_exists('t_user', 'name', 'name VARCHAR(50) COMMENT \'用户真实姓名\'');

-- ============================================================
-- 角色表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_role (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code         VARCHAR(50)  NOT NULL COMMENT '角色编码',
    name         VARCHAR(50)  NOT NULL COMMENT '角色名称',
    description  VARCHAR(200) COMMENT '角色描述',
    is_protected SMALLINT     NOT NULL DEFAULT 0  COMMENT '0=可删除, 1=系统保护不可删除',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================================
-- 权限表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_permission (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code        VARCHAR(50)  NOT NULL COMMENT '权限编码',
    name        VARCHAR(50)  NOT NULL COMMENT '权限名称',
    description VARCHAR(200) COMMENT '权限描述',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX idx_permission_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ============================================================
-- 用户-角色关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_user_role (
    id         BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id    BIGINT    NOT NULL COMMENT '用户ID',
    role_id    BIGINT    NOT NULL COMMENT '角色ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX idx_user_role (user_id, role_id),
    INDEX idx_ur_user_id (user_id),
    INDEX idx_ur_role_id (role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES t_role(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

-- ============================================================
-- 角色-权限关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_role_permission (
    id            BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_id       BIGINT    NOT NULL COMMENT '角色ID',
    permission_id BIGINT    NOT NULL COMMENT '权限ID',
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX idx_role_permission (role_id, permission_id),
    INDEX idx_rp_role_id (role_id),
    INDEX idx_rp_permission_id (permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES t_role(id),
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES t_permission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限关联表';

-- ============================================================
-- 项目表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_project (
    id                 BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code               VARCHAR(20)  NOT NULL COMMENT '项目编码',
    name               VARCHAR(100) NOT NULL COMMENT '项目名称',
    type               SMALLINT     NOT NULL COMMENT '项目类型',
    location           VARCHAR(200) COMMENT '项目地点',
    client_name        VARCHAR(100) COMMENT '客户名称',
    supervisor_unit    VARCHAR(100) COMMENT '监理单位',
    project_manager_id BIGINT       COMMENT '项目经理ID',
    planned_start_date DATE         COMMENT '计划开始日期',
    planned_end_date   DATE         COMMENT '计划结束日期',
    actual_start_date  DATE         COMMENT '实际开始日期',
    actual_end_date    DATE         COMMENT '实际结束日期',
    status             SMALLINT     NOT NULL DEFAULT 0  COMMENT '0=筹备中,1=在建,2=停工,3=已竣工,4=已结算',
    remark             VARCHAR(500) COMMENT '备注',
    is_deleted         SMALLINT     NOT NULL DEFAULT 0  COMMENT '0=正常,1=已删除',
    created_by         BIGINT       COMMENT '创建人ID',
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_project_code (code),
    INDEX idx_project_status (status),
    INDEX idx_project_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- ============================================================
-- 采购申请表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_purchase_request (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code          VARCHAR(30)  COMMENT '采购申请编码',
    project_id    BIGINT       NOT NULL COMMENT '项目ID',
    name          VARCHAR(100) NOT NULL COMMENT '采购名称',
    specification VARCHAR(200) COMMENT '规格型号',
    brand         VARCHAR(100) COMMENT '品牌',
    quantity      DOUBLE       NOT NULL DEFAULT 0  COMMENT '数量',
    unit          SMALLINT     NOT NULL COMMENT '单位',
    arrival_date  DATE         COMMENT '预计到货日期',
    status        SMALLINT     NOT NULL DEFAULT 0  COMMENT '0=待提交,1=审批中,2=已通过,3=已驳回,4=已取消',
    remark        VARCHAR(500) COMMENT '备注',
    is_deleted    SMALLINT     NOT NULL DEFAULT 0  COMMENT '0=正常,1=已删除',
    created_by    BIGINT       COMMENT '创建人ID',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_pr_project_id (project_id),
    INDEX idx_pr_status (status),
    INDEX idx_pr_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购申请表';

-- DDL 增量变更：采购申请关联物料
CALL proc_add_column_if_not_exists('t_purchase_request', 'material_id', 'material_id BIGINT NOT NULL COMMENT \'物料ID\'');
-- 为已有表补建索引（忽略重复索引错误）
ALTER TABLE t_purchase_request ADD INDEX idx_pr_material (material_id);
-- 为已有表补建外键（忽略重复外键错误）
ALTER TABLE t_purchase_request ADD CONSTRAINT fk_pr_material FOREIGN KEY (material_id) REFERENCES t_material(id);

-- ============================================================
-- 工作流实例关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_workflow_instance (
    id                  BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    business_type       VARCHAR(50) NOT NULL COMMENT '业务类型',
    business_id         BIGINT      NOT NULL COMMENT '业务记录ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    status              SMALLINT    NOT NULL DEFAULT 0  COMMENT '0=运行中,1=已完成,2=已终止',
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_wf_business (business_type, business_id),
    INDEX idx_wf_process_instance (process_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流实例关联表';

-- ============================================================
-- 仓库管理
-- ============================================================

-- 仓库表
CREATE TABLE IF NOT EXISTS t_warehouse (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code       VARCHAR(50)  NOT NULL COMMENT '仓库编码',
    name       VARCHAR(100) NOT NULL COMMENT '仓库名称',
    location   VARCHAR(200) COMMENT '仓库位置',
    status     SMALLINT     NOT NULL DEFAULT 1  COMMENT '1=启用, 0=禁用',
    is_deleted SMALLINT     NOT NULL DEFAULT 0  COMMENT '0=正常, 1=已删除',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_warehouse_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

-- 物料表
CREATE TABLE IF NOT EXISTS t_material (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code       VARCHAR(50)  NOT NULL COMMENT '物料编码',
    name       VARCHAR(100) NOT NULL COMMENT '物料名称',
    spec       VARCHAR(100) NOT NULL COMMENT '规格型号',
    unit       INTEGER     NOT NULL COMMENT '单位：0=个,1=台,2=套,3=米,4=吨,5=立方米,6=卷,7=桶,8=箱,9=其他',
    is_deleted SMALLINT     NOT NULL DEFAULT 0  COMMENT '0=正常, 1=已删除',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_material_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物料表';

-- 库存余量表
CREATE TABLE IF NOT EXISTS t_stock_balance (
    id           BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    warehouse_id BIGINT    NOT NULL COMMENT '仓库ID',
    material_id  BIGINT    NOT NULL COMMENT '物料ID',
    quantity     DOUBLE    NOT NULL DEFAULT 0  COMMENT '库存数量',
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_stock_wh_mat (warehouse_id, material_id),
    INDEX idx_sb_warehouse (warehouse_id),
    INDEX idx_sb_material (material_id),
    CONSTRAINT fk_sb_warehouse FOREIGN KEY (warehouse_id) REFERENCES t_warehouse(id),
    CONSTRAINT fk_sb_material FOREIGN KEY (material_id) REFERENCES t_material(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存余量表';

-- 出入库流水表
CREATE TABLE IF NOT EXISTS t_stock_record (
    id                  BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    warehouse_id        BIGINT    NOT NULL COMMENT '仓库ID',
    material_id         BIGINT    NOT NULL COMMENT '物料ID',
    type                SMALLINT  NOT NULL COMMENT '1=入库, 2=出库',
    quantity            DOUBLE    NOT NULL COMMENT '数量',
    purchase_request_id BIGINT    COMMENT '采购申请ID',
    project_id          BIGINT    COMMENT '项目ID',
    operator_id         BIGINT    COMMENT '操作人ID',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_sr_warehouse (warehouse_id),
    INDEX idx_sr_material (material_id),
    INDEX idx_sr_type (type),
    INDEX idx_sr_created_at (created_at),
    CONSTRAINT fk_sr_warehouse FOREIGN KEY (warehouse_id) REFERENCES t_warehouse(id),
    CONSTRAINT fk_sr_material FOREIGN KEY (material_id) REFERENCES t_material(id),
    CONSTRAINT fk_sr_operator FOREIGN KEY (operator_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出入库流水表';

-- ============================================================
-- 临时工管理
-- ============================================================
CREATE TABLE IF NOT EXISTS t_temp_worker (
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id     BIGINT      NOT NULL COMMENT '用户ID',
    project_id  BIGINT      NOT NULL COMMENT '项目ID',
    worker_type VARCHAR(50) COMMENT '工种',
    entry_date  DATE        NOT NULL COMMENT '入职日期',
    exit_date   DATE        COMMENT '离职日期',
    status      SMALLINT    NOT NULL DEFAULT 1  COMMENT '1=在职, 0=离职',
    is_deleted  SMALLINT    NOT NULL DEFAULT 0  COMMENT '0=正常, 1=已删除',
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_tw_user_id (user_id),
    INDEX idx_tw_project_id (project_id),
    INDEX idx_tw_status (status),
    CONSTRAINT fk_tw_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_tw_project FOREIGN KEY (project_id) REFERENCES t_project(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='临时工表';

-- ============================================================
-- 打卡模块
-- ============================================================
CREATE TABLE IF NOT EXISTS t_attendance (
    id         BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    worker_id  BIGINT    NOT NULL COMMENT '临时工ID',
    project_id BIGINT    NOT NULL COMMENT '项目ID',
    clock_in   TIMESTAMP NOT NULL COMMENT '上班打卡时间',
    clock_out  TIMESTAMP COMMENT '下班打卡时间',
    status     SMALLINT  NOT NULL DEFAULT 1  COMMENT '1=已上班打卡, 2=已下班打卡',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_att_worker (worker_id),
    INDEX idx_att_project (project_id),
    INDEX idx_att_status (status),
    CONSTRAINT fk_att_worker FOREIGN KEY (worker_id) REFERENCES t_temp_worker(id),
    CONSTRAINT fk_att_project FOREIGN KEY (project_id) REFERENCES t_project(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡记录表';

-- 打卡记录新增位置字段（DDL 增量变更，使用存储过程保证幂等）
CALL proc_add_column_if_not_exists('t_attendance', 'clock_in_lat',  'clock_in_lat  DECIMAL(10,7) COMMENT \'上班打卡纬度（GCJ-02）\'');
CALL proc_add_column_if_not_exists('t_attendance', 'clock_in_lng',  'clock_in_lng  DECIMAL(10,7) COMMENT \'上班打卡经度（GCJ-02）\'');
CALL proc_add_column_if_not_exists('t_attendance', 'clock_out_lat', 'clock_out_lat DECIMAL(10,7) COMMENT \'下班打卡纬度（GCJ-02）\'');
CALL proc_add_column_if_not_exists('t_attendance', 'clock_out_lng', 'clock_out_lng DECIMAL(10,7) COMMENT \'下班打卡经度（GCJ-02）\'');
CALL proc_add_column_if_not_exists('t_attendance', 'location_risk',  'location_risk SMALLINT NOT NULL DEFAULT 0 COMMENT \'定位风险等级：0=正常, 1=可疑, 2=高风险, 3=严重\'');

-- ============================================================
-- 打卡区域管理
-- ============================================================
CREATE TABLE IF NOT EXISTS t_attendance_area (
    id         BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id BIGINT        NOT NULL COMMENT '项目ID',
    name       VARCHAR(100)  NOT NULL COMMENT '区域名称',
    center_lat DECIMAL(10,7) NOT NULL COMMENT '区域中心纬度（GCJ-02）',
    center_lng DECIMAL(10,7) NOT NULL COMMENT '区域中心经度（GCJ-02）',
    radius     INTEGER       NOT NULL COMMENT '打卡半径（米），范围 50-5000',
    is_deleted SMALLINT      NOT NULL DEFAULT 0  COMMENT '0=正常, 1=已删除',
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_att_area_project (project_id),
    CONSTRAINT fk_att_area_project FOREIGN KEY (project_id) REFERENCES t_project(id),
    CONSTRAINT chk_radius CHECK (radius >= 50 AND radius <= 5000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡区域配置表';

-- ============================================================
-- 通用文件上传
-- ============================================================
CREATE TABLE IF NOT EXISTS t_upload_file (
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    business_type VARCHAR(50)  COMMENT '业务类型',
    business_id   BIGINT       COMMENT '业务记录ID',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_path   VARCHAR(500) NOT NULL COMMENT '存储路径',
    file_size     BIGINT       NOT NULL DEFAULT 0  COMMENT '文件大小（字节）',
    content_type  VARCHAR(100) COMMENT 'MIME类型',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_upload_business (business_type, business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用上传文件表';

-- ============================================================
-- 微信小程序用户绑定
-- ============================================================
CREATE TABLE IF NOT EXISTS t_user_wechat (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id    BIGINT       NOT NULL COMMENT '用户ID',
    openid     VARCHAR(100) NOT NULL COMMENT '微信小程序 openid',
    unionid    VARCHAR(100) COMMENT '微信开放平台 unionid（可选）',
    nickname   VARCHAR(100) COMMENT '微信昵称',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX idx_uw_user_id (user_id),
    UNIQUE INDEX idx_uw_openid (openid),
    CONSTRAINT fk_uw_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-微信绑定表';

-- ============================================================
-- 建筑废渣管理
-- ============================================================
CREATE TABLE IF NOT EXISTS t_construction_waste (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id     BIGINT       NOT NULL COMMENT '项目ID',
    waste_type     VARCHAR(100) NOT NULL COMMENT '废渣类型',
    quantity       DOUBLE       NOT NULL DEFAULT 0  COMMENT '数量',
    unit           VARCHAR(20)  NOT NULL COMMENT '单位',
    vehicle_plate  VARCHAR(20)  COMMENT '车牌号',
    driver_name    VARCHAR(50)  COMMENT '驾驶员姓名',
    destination    VARCHAR(200) COMMENT '运往目的地',
    removal_date   DATE         NOT NULL COMMENT '运出日期',
    remark         VARCHAR(500) COMMENT '备注',
    operator_id    BIGINT       COMMENT '操作人ID',
    is_deleted     SMALLINT     NOT NULL DEFAULT 0  COMMENT '0=正常, 1=已删除',
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_cw_project (project_id),
    INDEX idx_cw_removal_date (removal_date),
    INDEX idx_cw_is_deleted (is_deleted),
    CONSTRAINT fk_cw_project FOREIGN KEY (project_id) REFERENCES t_project(id),
    CONSTRAINT fk_cw_operator FOREIGN KEY (operator_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='建筑废渣运出记录表';

-- ============================================================
-- 采购记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_purchase_order (
    id                    BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    purchase_request_id   BIGINT        COMMENT '关联采购申请ID，1:1',
    supplier_name         VARCHAR(100)  COMMENT '供应商名称',
    supplier_phone        VARCHAR(30)   COMMENT '供应商电话',
    unit_price            DECIMAL(12,2) COMMENT '单价',
    total_amount          DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '总金额',
    purchase_date         DATE          COMMENT '采购日期',
    expected_arrival_date DATE          COMMENT '预计到货日期',
    status                SMALLINT      NOT NULL DEFAULT 0  COMMENT '0=采购中, 1=已采购, 2=已到货',
    remark                VARCHAR(500)  COMMENT '备注',
    is_deleted            SMALLINT      NOT NULL DEFAULT 0  COMMENT '0=正常, 1=已删除',
    created_by            BIGINT        COMMENT '创建人ID',
    created_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_po_status (status),
    INDEX idx_po_is_deleted (is_deleted),
    UNIQUE INDEX idx_po_pr (purchase_request_id),
    CONSTRAINT fk_po_created_by FOREIGN KEY (created_by) REFERENCES t_user(id),
    CONSTRAINT fk_po_pr FOREIGN KEY (purchase_request_id) REFERENCES t_purchase_request(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购记录表';

-- DDL 增量变更：采购记录 1:1 关联采购申请（幂等，已有表时生效）
CALL proc_add_column_if_not_exists('t_purchase_order', 'purchase_request_id', 'purchase_request_id BIGINT COMMENT \'关联采购申请ID，1:1\'');
-- 为已有表补建索引和外键（若已存在则忽略错误）
ALTER TABLE t_purchase_order MODIFY COLUMN supplier_name VARCHAR(100) NULL COMMENT '供应商名称';
ALTER TABLE t_purchase_order MODIFY COLUMN purchase_date DATE NULL COMMENT '采购日期';
ALTER TABLE t_purchase_order MODIFY COLUMN total_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '总金额';
-- 清理旧关联表
DROP TABLE IF EXISTS t_purchase_order_item;

-- ============================================================
-- 供应商管理
-- ============================================================
CREATE TABLE IF NOT EXISTS t_supplier (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id         BIGINT       NOT NULL COMMENT '关联用户ID，1:1关系',
    company_name    VARCHAR(200) NOT NULL COMMENT '公司名称',
    license_no      VARCHAR(100) COMMENT '统一社会信用代码',
    contact_person  VARCHAR(50)  NOT NULL COMMENT '联系人',
    contact_phone   VARCHAR(30)  NOT NULL COMMENT '联系电话',
    address         VARCHAR(300) COMMENT '公司地址',
    status          SMALLINT     NOT NULL DEFAULT 1  COMMENT '1=正常, 0=禁用',
    remark          VARCHAR(500) COMMENT '备注',
    is_deleted      SMALLINT     NOT NULL DEFAULT 0  COMMENT '0=正常, 1=已删除',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_supplier_user (user_id),
    INDEX idx_supplier_company (company_name),
    INDEX idx_supplier_status (status),
    CONSTRAINT fk_supplier_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';

-- ============================================================
-- 预置种子数据：角色、权限、角色-权限关联
-- ============================================================

-- 角色
INSERT IGNORE INTO t_role (code, name, description, is_protected) VALUES
    ('admin',            '管理员',   '系统管理员，拥有所有权限', 1),
    ('project_manager',  '项目经理', '负责项目管理',            0),
    ('finance',          '财务',     '负责采购审批和财务管理',   0),
    ('user',             '普通用户', '基础查看权限',            0),
    ('temp_worker',      '临时工',   '临时工，仅可登录和打卡',   0),
    ('supplier',         '供应商',   '供应商，可注册和管理自身企业资料', 0);

-- 权限
INSERT IGNORE INTO t_permission (code, name, description) VALUES
    ('user:read',                '查看用户',         '查看用户列表和详情'),
    ('user:create',              '创建用户',         '创建新用户'),
    ('user:update',              '编辑用户',         '编辑用户信息和状态'),
    ('user:delete',              '删除用户',         '软删除用户'),
    ('user:role:assign',         '分配角色',         '为用户分配角色'),
    ('role:read',                '查看角色',         '查看角色列表'),
    ('project:read',             '查看项目',         '查看项目列表和详情'),
    ('project:create',           '创建项目',         '创建新项目'),
    ('project:update',           '编辑项目',         '编辑项目信息'),
    ('project:delete',           '删除项目',         '删除项目'),
    ('pr:read',                  '查看采购申请',     '查看采购申请列表和详情'),
    ('pr:create',                '创建采购申请',     '创建新采购申请'),
    ('pr:approve',               '审批采购申请',     '审批采购申请'),
    ('task:read',                '查看待办任务',     '查看待办任务列表'),
    ('task:complete',            '完成任务',         '完成审批任务'),
    ('role:create',              '创建角色',         '创建新角色'),
    ('role:update',              '编辑角色',         '编辑角色信息和权限'),
    ('role:delete',              '删除角色',         '删除非保护角色'),
    ('role:permission:assign',   '分配权限',         '为角色分配权限'),
    ('warehouse:read',           '查看仓库',         '查看仓库列表'),
    ('warehouse:create',         '创建仓库',         '创建新仓库'),
    ('warehouse:update',         '编辑仓库',         '编辑仓库信息'),
    ('warehouse:delete',         '删除仓库',         '删除仓库'),
    ('material:read',            '查看物料',         '查看物料列表'),
    ('material:create',          '创建物料',         '创建新物料'),
    ('material:update',          '编辑物料',         '编辑物料信息'),
    ('material:delete',          '删除物料',         '删除物料'),
    ('stock:in',                 '入库',             '入库操作'),
    ('stock:out',                '出库',             '出库操作'),
    ('attendance:clock-in',      '上班打卡',         '上班打卡'),
    ('attendance:clock-out',     '下班打卡',         '下班打卡'),
    ('attendance:area-manage',   '管理打卡区域',     '配置项目的打卡范围'),
    ('waste:read',               '查看建筑废渣',     '查看建筑废渣运出记录列表和详情'),
    ('waste:create',             '创建建筑废渣记录', '创建建筑废渣运出记录并上传照片'),
    ('waste:delete',             '删除建筑废渣记录', '删除建筑废渣运出记录及关联文件'),
    ('purchase:read',            '查看采购记录',     '查看采购记录列表和详情'),
    ('purchase:create',          '创建采购记录',     '创建新采购记录'),
    ('purchase:update',          '编辑采购记录',     '编辑采购记录信息和状态'),
    ('purchase:delete',          '删除采购记录',     '删除采购记录及关联文件'),
    ('supplier:read',            '查看供应商',       '查看供应商列表和详情'),
    ('supplier:create',          '创建供应商',       '手动创建供应商'),
    ('supplier:update',          '编辑供应商',       '编辑供应商信息'),
    ('supplier:delete',          '删除供应商',       '删除供应商'),
    ('supplier:register',        '供应商注册',       '通过小程序注册为供应商'),
    ('supplier:profile',         '供应商资料',       '查看和编辑自己的供应商资料');

-- -----------------------------------------------------------
-- 种子数据：角色-权限关联 存储过程
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_seed_role_permissions;

DELIMITER //
CREATE PROCEDURE proc_seed_role_permissions()
BEGIN
    DECLARE v_admin_id        BIGINT;
    DECLARE v_pm_id           BIGINT;
    DECLARE v_finance_id      BIGINT;
    DECLARE v_user_id         BIGINT;
    DECLARE v_temp_worker_id  BIGINT;
    DECLARE v_supplier_id     BIGINT;
    DECLARE v_done            INT DEFAULT FALSE;
    DECLARE v_perm_id         BIGINT;

    -- 游标：遍历所有权限
    DECLARE perm_cursor CURSOR FOR SELECT id FROM t_permission;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;

    SELECT id INTO v_admin_id       FROM t_role WHERE code = 'admin';
    SELECT id INTO v_pm_id          FROM t_role WHERE code = 'project_manager';
    SELECT id INTO v_finance_id     FROM t_role WHERE code = 'finance';
    SELECT id INTO v_user_id        FROM t_role WHERE code = 'user';
    SELECT id INTO v_temp_worker_id FROM t_role WHERE code = 'temp_worker';
    SELECT id INTO v_supplier_id    FROM t_role WHERE code = 'supplier';

    -- admin: 所有权限
    OPEN perm_cursor;
    perm_loop: LOOP
        FETCH perm_cursor INTO v_perm_id;
        IF v_done THEN
            LEAVE perm_loop;
        END IF;
        INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES (v_admin_id, v_perm_id);
    END LOOP;
    CLOSE perm_cursor;

    -- project_manager: 用户查看 + 项目全部 + 采购查看 + 任务查看
    INSERT IGNORE INTO t_role_permission (role_id, permission_id)
    SELECT v_pm_id, id FROM t_permission
    WHERE code IN ('user:read', 'project:read', 'project:create', 'project:update', 'project:delete', 'pr:read', 'task:read');

    -- finance: 用户查看 + 项目查看 + 采购全部 + 任务全部
    INSERT IGNORE INTO t_role_permission (role_id, permission_id)
    SELECT v_finance_id, id FROM t_permission
    WHERE code IN ('user:read', 'project:read', 'pr:read', 'pr:create', 'pr:approve', 'task:read', 'task:complete');

    -- user: 只读权限（含角色查看）
    INSERT IGNORE INTO t_role_permission (role_id, permission_id)
    SELECT v_user_id, id FROM t_permission
    WHERE code IN ('user:read', 'role:read', 'project:read', 'pr:read', 'task:read');

    -- temp_worker: 最小权限（查看自身 + 打卡）
    INSERT IGNORE INTO t_role_permission (role_id, permission_id)
    SELECT v_temp_worker_id, id FROM t_permission
    WHERE code IN ('user:read', 'attendance:clock-in', 'attendance:clock-out');

    -- supplier: 注册权限 + 自身资料管理
    INSERT IGNORE INTO t_role_permission (role_id, permission_id)
    SELECT v_supplier_id, id FROM t_permission
    WHERE code IN ('supplier:register', 'supplier:profile');

    -- 用户-角色关联：给已有的 admin 用户分配管理员角色
    INSERT IGNORE INTO t_user_role (user_id, role_id)
    SELECT u.id, v_admin_id
    FROM t_user u
    WHERE u.username = 'admin'
      AND NOT EXISTS (SELECT 1 FROM t_user_role ur WHERE ur.user_id = u.id AND ur.role_id = v_admin_id);
END //
DELIMITER ;

-- 执行种子数据存储过程
CALL proc_seed_role_permissions();
DROP PROCEDURE IF EXISTS proc_seed_role_permissions;

-- -----------------------------------------------------------
-- 清理辅助存储过程
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS proc_add_column_if_not_exists;

INSERT INTO t_user(username, name, password_hash)
VALUES ('admin', 'admin','$2a$10$E2s24RNIXqcNlEY/WDzx2.JxAa7TaxuOTgL0J.XZO6M6.VZWaMu.O');

INSERT INTO t_user_role(user_id, role_id, created_at)
VALUES (1, 1, now());
-- ============================================================
-- MySQL 说明：
-- 1. BIGINT AUTO_INCREMENT 替代 PostgreSQL 的 BIGSERIAL，无需手动管理序列
-- 2. 使用 INSERT IGNORE 替代 ON CONFLICT DO NOTHING 实现幂等插入
-- 3. 使用存储过程替代 PostgreSQL 的 DO $$ 匿名块
-- 4. updated_at 通过 ON UPDATE CURRENT_TIMESTAMP 自动更新，无需触发器
-- 5. 部分索引（WHERE is_deleted = 0）已替换为普通索引
-- 6. 运行时需设置 SQL_MODE 确保数据完整性：
--    SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';
-- ============================================================
