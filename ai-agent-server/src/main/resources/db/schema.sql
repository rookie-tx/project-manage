CREATE TABLE IF NOT EXISTS t_user (
    id          BIGSERIAL    PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email       VARCHAR(100),
    phone       VARCHAR(20),
    status                SMALLINT     NOT NULL DEFAULT 1,
    must_change_password  SMALLINT     NOT NULL DEFAULT 0,
    is_deleted            SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_user IS '用户表';
COMMENT ON COLUMN t_user.status IS '1=正常, 0=禁用';
COMMENT ON COLUMN t_user.must_change_password IS '0=无需改密, 1=首次登录需改密';
COMMENT ON COLUMN t_user.is_deleted IS '0=正常, 1=已删除';

-- DDL 增量变更：用户真实姓名
ALTER TABLE t_user ADD COLUMN IF NOT EXISTS name VARCHAR(50);
COMMENT ON COLUMN t_user.name IS '用户真实姓名';

-- 角色表
CREATE TABLE IF NOT EXISTS t_role (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(200),
    is_protected SMALLINT    NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_role IS '角色表';
COMMENT ON COLUMN t_role.is_protected IS '0=可删除, 1=系统保护不可删除';

CREATE TRIGGER trg_t_role_updated_at
    BEFORE UPDATE ON t_role
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 权限表
CREATE TABLE IF NOT EXISTS t_permission (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(200),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_permission IS '权限表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS t_user_role (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES t_user(id),
    role_id     BIGINT       NOT NULL REFERENCES t_role(id),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, role_id)
);

COMMENT ON TABLE t_user_role IS '用户-角色关联表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS t_role_permission (
    id              BIGSERIAL    PRIMARY KEY,
    role_id         BIGINT       NOT NULL REFERENCES t_role(id),
    permission_id   BIGINT       NOT NULL REFERENCES t_permission(id),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role_id, permission_id)
);

COMMENT ON TABLE t_role_permission IS '角色-权限关联表';

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_t_user_updated_at
    BEFORE UPDATE ON t_user
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 项目表
CREATE TABLE IF NOT EXISTS t_project (
    id                  BIGSERIAL       PRIMARY KEY,
    code                VARCHAR(20)     NOT NULL UNIQUE,
    name                VARCHAR(100)    NOT NULL,
    type                SMALLINT        NOT NULL,
    location            VARCHAR(200),
    client_name         VARCHAR(100),
    supervisor_unit     VARCHAR(100),
    project_manager_id  BIGINT,
    planned_start_date  DATE,
    planned_end_date    DATE,
    actual_start_date   DATE,
    actual_end_date     DATE,
    status              SMALLINT        NOT NULL DEFAULT 0,
    remark              VARCHAR(500),
    is_deleted          SMALLINT        NOT NULL DEFAULT 0,
    created_by          BIGINT,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_project IS '项目表';
COMMENT ON COLUMN t_project.status IS '0=筹备中,1=在建,2=停工,3=已竣工,4=已结算';
COMMENT ON COLUMN t_project.is_deleted IS '0=正常,1=已删除';

CREATE TRIGGER trg_t_project_updated_at
    BEFORE UPDATE ON t_project
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 采购申请表
CREATE TABLE IF NOT EXISTS t_purchase_request (
    id              BIGSERIAL       PRIMARY KEY,
    code            VARCHAR(30),
    project_id      BIGINT          NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    specification   VARCHAR(200),
    brand           VARCHAR(100),
    quantity        DOUBLE PRECISION NOT NULL DEFAULT 0,
    unit            SMALLINT        NOT NULL,
    arrival_date    DATE,
    status          SMALLINT        NOT NULL DEFAULT 0,
    remark          VARCHAR(500),
    is_deleted      SMALLINT        NOT NULL DEFAULT 0,
    created_by      BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_purchase_request IS '采购申请表';
COMMENT ON COLUMN t_purchase_request.status IS '0=待提交,1=审批中,2=已通过,3=已驳回,4=已取消';
COMMENT ON COLUMN t_purchase_request.is_deleted IS '0=正常,1=已删除';

CREATE TRIGGER trg_t_purchase_request_updated_at
    BEFORE UPDATE ON t_purchase_request
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 工作流实例关联表
CREATE TABLE IF NOT EXISTS t_workflow_instance (
    id                  BIGSERIAL       PRIMARY KEY,
    business_type       VARCHAR(50)     NOT NULL,
    business_id         BIGINT          NOT NULL,
    process_instance_id VARCHAR(64)     NOT NULL,
    status              SMALLINT        NOT NULL DEFAULT 0,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_workflow_instance IS '工作流实例关联表';
COMMENT ON COLUMN t_workflow_instance.status IS '0=运行中,1=已完成,2=已终止';

CREATE UNIQUE INDEX IF NOT EXISTS idx_wf_business ON t_workflow_instance(business_type, business_id);

CREATE TRIGGER trg_t_workflow_instance_updated_at
    BEFORE UPDATE ON t_workflow_instance
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- 仓库管理
-- ============================================================

-- 仓库表
CREATE TABLE IF NOT EXISTS t_warehouse (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    location    VARCHAR(200),
    status      SMALLINT     NOT NULL DEFAULT 1,
    is_deleted  SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_warehouse IS '仓库表';
COMMENT ON COLUMN t_warehouse.status IS '1=启用, 0=禁用';

CREATE TRIGGER trg_t_warehouse_updated_at
    BEFORE UPDATE ON t_warehouse
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 物料表
CREATE TABLE IF NOT EXISTS t_material (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    spec        VARCHAR(100),
    unit        VARCHAR(20),
    is_deleted  SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_material IS '物料表';

CREATE TRIGGER trg_t_material_updated_at
    BEFORE UPDATE ON t_material
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 库存余量表
CREATE TABLE IF NOT EXISTS t_stock_balance (
    id            BIGSERIAL    PRIMARY KEY,
    warehouse_id  BIGINT       NOT NULL REFERENCES t_warehouse(id),
    material_id   BIGINT       NOT NULL REFERENCES t_material(id),
    quantity      DOUBLE PRECISION NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(warehouse_id, material_id)
);

COMMENT ON TABLE t_stock_balance IS '库存余量表';

CREATE TRIGGER trg_t_stock_balance_updated_at
    BEFORE UPDATE ON t_stock_balance
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 出入库流水表
CREATE TABLE IF NOT EXISTS t_stock_record (
    id                  BIGSERIAL    PRIMARY KEY,
    warehouse_id        BIGINT       NOT NULL REFERENCES t_warehouse(id),
    material_id         BIGINT       NOT NULL REFERENCES t_material(id),
    type                SMALLINT     NOT NULL,
    quantity            DOUBLE PRECISION NOT NULL,
    purchase_request_id BIGINT,
    project_id          BIGINT,
    operator_id         BIGINT       REFERENCES t_user(id),
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_stock_record IS '出入库流水表';
COMMENT ON COLUMN t_stock_record.type IS '1=入库, 2=出库';

-- ============================================================
-- 临时工管理
-- ============================================================

CREATE TABLE IF NOT EXISTS t_temp_worker (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL UNIQUE REFERENCES t_user(id),
    project_id  BIGINT       NOT NULL REFERENCES t_project(id),
    worker_type VARCHAR(50),
    entry_date  DATE         NOT NULL,
    exit_date   DATE,
    status      SMALLINT     NOT NULL DEFAULT 1,
    is_deleted  SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_temp_worker IS '临时工表';
COMMENT ON COLUMN t_temp_worker.status IS '1=在职, 0=离职';

CREATE TRIGGER trg_t_temp_worker_updated_at
    BEFORE UPDATE ON t_temp_worker
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- 打卡模块
-- ============================================================

CREATE TABLE IF NOT EXISTS t_attendance (
    id          BIGSERIAL    PRIMARY KEY,
    worker_id   BIGINT       NOT NULL REFERENCES t_temp_worker(id),
    project_id  BIGINT       NOT NULL REFERENCES t_project(id),
    clock_in    TIMESTAMP    NOT NULL,
    clock_out   TIMESTAMP,
    status      SMALLINT     NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_attendance IS '打卡记录表';
COMMENT ON COLUMN t_attendance.status IS '1=已上班打卡, 2=已下班打卡';

CREATE TRIGGER trg_t_attendance_updated_at
    BEFORE UPDATE ON t_attendance
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 打卡记录新增位置字段（DDL 增量变更，已存在表无则跳过异常）
ALTER TABLE t_attendance ADD COLUMN IF NOT EXISTS clock_in_lat  DECIMAL(10,7);
ALTER TABLE t_attendance ADD COLUMN IF NOT EXISTS clock_in_lng  DECIMAL(10,7);
ALTER TABLE t_attendance ADD COLUMN IF NOT EXISTS clock_out_lat DECIMAL(10,7);
ALTER TABLE t_attendance ADD COLUMN IF NOT EXISTS clock_out_lng DECIMAL(10,7);
ALTER TABLE t_attendance ADD COLUMN IF NOT EXISTS location_risk SMALLINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN t_attendance.clock_in_lat IS '上班打卡纬度（GCJ-02）';
COMMENT ON COLUMN t_attendance.clock_in_lng IS '上班打卡经度（GCJ-02）';
COMMENT ON COLUMN t_attendance.clock_out_lat IS '下班打卡纬度（GCJ-02）';
COMMENT ON COLUMN t_attendance.clock_out_lng IS '下班打卡经度（GCJ-02）';
COMMENT ON COLUMN t_attendance.location_risk IS '定位风险等级：0=正常, 1=可疑, 2=高风险, 3=严重';

-- ============================================================
-- 打卡区域管理
-- ============================================================

CREATE TABLE IF NOT EXISTS t_attendance_area (
    id          BIGSERIAL    PRIMARY KEY,
    project_id  BIGINT       NOT NULL REFERENCES t_project(id),
    name        VARCHAR(100) NOT NULL,
    center_lat  DECIMAL(10,7) NOT NULL,
    center_lng  DECIMAL(10,7) NOT NULL,
    radius      INTEGER      NOT NULL CHECK (radius >= 50 AND radius <= 5000),
    is_deleted  SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_attendance_area IS '打卡区域配置表';
COMMENT ON COLUMN t_attendance_area.center_lat IS '区域中心纬度（GCJ-02）';
COMMENT ON COLUMN t_attendance_area.center_lng IS '区域中心经度（GCJ-02）';
COMMENT ON COLUMN t_attendance_area.radius IS '打卡半径（米），范围 50-5000';
COMMENT ON COLUMN t_attendance_area.is_deleted IS '0=正常, 1=已删除';

CREATE INDEX IF NOT EXISTS idx_attendance_area_project ON t_attendance_area(project_id) WHERE is_deleted = 0;

CREATE TRIGGER trg_t_attendance_area_updated_at
    BEFORE UPDATE ON t_attendance_area
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- 通用文件上传
-- ============================================================

CREATE TABLE IF NOT EXISTS t_upload_file (
    id              BIGSERIAL    PRIMARY KEY,
    business_type   VARCHAR(50),
    business_id     BIGINT,
    original_name   VARCHAR(255) NOT NULL,
    stored_path     VARCHAR(500) NOT NULL,
    file_size       BIGINT       NOT NULL DEFAULT 0,
    content_type    VARCHAR(100),
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_upload_file IS '通用上传文件表';
COMMENT ON COLUMN t_upload_file.business_type IS '业务类型';
COMMENT ON COLUMN t_upload_file.business_id IS '业务记录ID';

CREATE INDEX IF NOT EXISTS idx_upload_file_business ON t_upload_file(business_type, business_id);

-- ============================================================
-- 微信小程序用户绑定
-- ============================================================

CREATE TABLE IF NOT EXISTS t_user_wechat (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL UNIQUE REFERENCES t_user(id),
    openid      VARCHAR(100) NOT NULL UNIQUE,
    unionid     VARCHAR(100),
    nickname    VARCHAR(100),
    avatar_url  VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_user_wechat IS '用户-微信绑定表';
COMMENT ON COLUMN t_user_wechat.openid IS '微信小程序 openid';
COMMENT ON COLUMN t_user_wechat.unionid IS '微信开放平台 unionid（可选）';

-- ============================================================
-- 建筑废渣管理
-- ============================================================

CREATE TABLE IF NOT EXISTS t_construction_waste (
    id              BIGSERIAL       PRIMARY KEY,
    project_id      BIGINT          NOT NULL REFERENCES t_project(id),
    waste_type      VARCHAR(100)    NOT NULL,
    quantity        DOUBLE PRECISION NOT NULL DEFAULT 0,
    unit            VARCHAR(20)     NOT NULL,
    vehicle_plate   VARCHAR(20),
    driver_name     VARCHAR(50),
    destination     VARCHAR(200),
    removal_date    DATE            NOT NULL,
    remark          VARCHAR(500),
    operator_id     BIGINT          REFERENCES t_user(id),
    is_deleted      SMALLINT        NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_construction_waste IS '建筑废渣运出记录表';
COMMENT ON COLUMN t_construction_waste.is_deleted IS '0=正常, 1=已删除';

CREATE TRIGGER trg_t_construction_waste_updated_at
    BEFORE UPDATE ON t_construction_waste
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE IF NOT EXISTS t_purchase_order (
    id                  BIGSERIAL       PRIMARY KEY,
    purchase_request_id BIGINT          UNIQUE REFERENCES t_purchase_request(id),
    supplier_name       VARCHAR(100),
    supplier_phone      VARCHAR(30),
    unit_price           DECIMAL(12,2),
    total_amount         DECIMAL(14,2)  NOT NULL DEFAULT 0,
    purchase_date       DATE,
    expected_arrival_date DATE,
    status              SMALLINT        NOT NULL DEFAULT 0,
    remark              VARCHAR(500),
    is_deleted          SMALLINT        NOT NULL DEFAULT 0,
    created_by          BIGINT          REFERENCES t_user(id),
    purchaser_id        BIGINT          REFERENCES t_user(id),
    inbound_operator_id BIGINT          REFERENCES t_user(id),
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_purchase_order IS '采购记录表';
COMMENT ON COLUMN t_purchase_order.status IS '0=采购中, 1=已采购, 2=已到货';
COMMENT ON COLUMN t_purchase_order.is_deleted IS '0=正常, 1=已删除';
COMMENT ON COLUMN t_purchase_order.purchaser_id IS '采购人ID';
COMMENT ON COLUMN t_purchase_order.inbound_operator_id IS '入库人ID';

-- DDL 增量变更：采购记录 1:1 关联采购申请
ALTER TABLE t_purchase_order ADD COLUMN IF NOT EXISTS purchase_request_id BIGINT;
ALTER TABLE t_purchase_order DROP CONSTRAINT IF EXISTS uq_po_pr;
ALTER TABLE t_purchase_order ADD CONSTRAINT uq_po_pr UNIQUE (purchase_request_id);
ALTER TABLE t_purchase_order ADD CONSTRAINT fk_po_pr FOREIGN KEY (purchase_request_id) REFERENCES t_purchase_request(id);
-- DDL 增量变更：供应商名称和采购日期改为可选（自动创建时留空）
ALTER TABLE t_purchase_order ALTER COLUMN supplier_name DROP NOT NULL;
ALTER TABLE t_purchase_order ALTER COLUMN purchase_date DROP NOT NULL;
ALTER TABLE t_purchase_order ALTER COLUMN total_amount SET DEFAULT 0;
-- DDL 增量变更：采购人和入库人字段
ALTER TABLE t_purchase_order ADD COLUMN IF NOT EXISTS purchaser_id BIGINT REFERENCES t_user(id);
ALTER TABLE t_purchase_order ADD COLUMN IF NOT EXISTS inbound_operator_id BIGINT REFERENCES t_user(id);

-- DDL 清理：删除 t_purchase_order_item 表
DROP TABLE IF EXISTS t_purchase_order_item;

CREATE TRIGGER trg_t_purchase_order_updated_at
    BEFORE UPDATE ON t_purchase_order
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- 供应商管理
-- ============================================================

CREATE TABLE IF NOT EXISTS t_supplier (
    id              BIGSERIAL    PRIMARY KEY,
    user_id         BIGINT       NOT NULL UNIQUE REFERENCES t_user(id),
    company_name    VARCHAR(200) NOT NULL,
    license_no      VARCHAR(100),
    contact_person  VARCHAR(50)  NOT NULL,
    contact_phone   VARCHAR(30)  NOT NULL,
    address         VARCHAR(300),
    status          SMALLINT     NOT NULL DEFAULT 1,
    remark          VARCHAR(500),
    is_deleted      SMALLINT     NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_supplier IS '供应商表';
COMMENT ON COLUMN t_supplier.user_id IS '关联用户ID，1:1关系';
COMMENT ON COLUMN t_supplier.company_name IS '公司名称';
COMMENT ON COLUMN t_supplier.license_no IS '统一社会信用代码';
COMMENT ON COLUMN t_supplier.contact_person IS '联系人';
COMMENT ON COLUMN t_supplier.contact_phone IS '联系电话';
COMMENT ON COLUMN t_supplier.address IS '公司地址';
COMMENT ON COLUMN t_supplier.status IS '1=正常, 0=禁用';
COMMENT ON COLUMN t_supplier.is_deleted IS '0=正常, 1=已删除';

CREATE TRIGGER trg_t_supplier_updated_at
    BEFORE UPDATE ON t_supplier
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- 预置种子数据：角色、权限、角色-权限关联
-- ============================================================

-- 角色
INSERT INTO t_role (code, name, description, is_protected) VALUES
    ('admin', '管理员', '系统管理员，拥有所有权限', 1),
    ('project_manager', '项目经理', '负责项目管理', 0),
    ('finance', '财务', '负责采购审批和财务管理', 0),
    ('user', '普通用户', '基础查看权限', 0),
    ('temp_worker', '临时工', '临时工，仅可登录和打卡', 0),
    ('supplier', '供应商', '供应商，可注册和管理自身企业资料', 0)
ON CONFLICT (code) DO NOTHING;

-- 如果 admin 角色已存在，确保 is_protected=1
UPDATE t_role SET is_protected = 1 WHERE code = 'admin';

-- 权限
INSERT INTO t_permission (code, name, description) VALUES
    ('user:read', '查看用户', '查看用户列表和详情'),
    ('user:create', '创建用户', '创建新用户'),
    ('user:update', '编辑用户', '编辑用户信息和状态'),
    ('user:delete', '删除用户', '软删除用户'),
    ('user:role:assign', '分配角色', '为用户分配角色'),
    ('role:read', '查看角色', '查看角色列表'),
    ('project:read', '查看项目', '查看项目列表和详情'),
    ('project:create', '创建项目', '创建新项目'),
    ('project:update', '编辑项目', '编辑项目信息'),
    ('project:delete', '删除项目', '删除项目'),
    ('pr:read', '查看采购申请', '查看采购申请列表和详情'),
    ('pr:create', '创建采购申请', '创建新采购申请'),
    ('pr:approve', '审批采购申请', '审批采购申请'),
    ('task:read', '查看待办任务', '查看待办任务列表'),
    ('task:complete', '完成任务', '完成审批任务'),
    ('role:create', '创建角色', '创建新角色'),
    ('role:update', '编辑角色', '编辑角色信息和权限'),
    ('role:delete', '删除角色', '删除非保护角色'),
    ('role:permission:assign', '分配权限', '为角色分配权限'),
    ('warehouse:read', '查看仓库', '查看仓库列表'),
    ('warehouse:create', '创建仓库', '创建新仓库'),
    ('warehouse:update', '编辑仓库', '编辑仓库信息'),
    ('warehouse:delete', '删除仓库', '删除仓库'),
    ('material:read', '查看物料', '查看物料列表'),
    ('material:create', '创建物料', '创建新物料'),
    ('material:update', '编辑物料', '编辑物料信息'),
    ('material:delete', '删除物料', '删除物料'),
    ('stock:in', '入库', '入库操作'),
    ('stock:out', '出库', '出库操作'),
    ('attendance:clock-in', '上班打卡', '上班打卡'),
    ('attendance:clock-out', '下班打卡', '下班打卡'),
    ('attendance:area-manage', '管理打卡区域', '配置项目的打卡范围'),
    ('waste:read', '查看建筑废渣', '查看建筑废渣运出记录列表和详情'),
    ('waste:create', '创建建筑废渣记录', '创建建筑废渣运出记录并上传照片'),
    ('waste:delete', '删除建筑废渣记录', '删除建筑废渣运出记录及关联文件'),
    ('purchase:read', '查看采购汇总', '查看全部采购记录列表'),
    ('purchase:pending:read', '查看待采购', '查看待采购列表'),
    ('purchase:purchased:read', '查看已采购', '查看已采购列表'),
    ('purchase:arrived:read', '查看已到货', '查看已到货列表'),
    ('purchase:create', '创建采购记录', '创建新采购记录'),
    ('purchase:update', '编辑采购记录', '编辑采购记录信息和状态'),
    ('purchase:delete', '删除采购记录', '删除采购记录及关联文件'),
    ('supplier:read', '查看供应商', '查看供应商列表和详情'),
    ('supplier:create', '创建供应商', '手动创建供应商'),
    ('supplier:update', '编辑供应商', '编辑供应商信息'),
    ('supplier:delete', '删除供应商', '删除供应商'),
    ('supplier:register', '供应商注册', '通过小程序注册为供应商'),
    ('supplier:profile', '供应商资料', '查看和编辑自己的供应商资料')
ON CONFLICT (code) DO NOTHING;

-- 角色-权限关联
DO $$
DECLARE
    admin_id BIGINT;
    pm_id BIGINT;
    finance_id BIGINT;
    user_id BIGINT;
    perm RECORD;
BEGIN
    SELECT id INTO admin_id FROM t_role WHERE code = 'admin';
    SELECT id INTO pm_id FROM t_role WHERE code = 'project_manager';
    SELECT id INTO finance_id FROM t_role WHERE code = 'finance';
    SELECT id INTO user_id FROM t_role WHERE code = 'user';

    -- admin: 所有权限
    FOR perm IN SELECT id FROM t_permission LOOP
        INSERT INTO t_role_permission (role_id, permission_id) VALUES (admin_id, perm.id) ON CONFLICT DO NOTHING;
    END LOOP;

    -- project_manager: 用户查看 + 项目全部 + 采购查看 + 任务查看
    FOR perm IN SELECT id FROM t_permission WHERE code IN ('user:read', 'project:read', 'project:create', 'project:update', 'project:delete', 'pr:read', 'task:read') LOOP
        INSERT INTO t_role_permission (role_id, permission_id) VALUES (pm_id, perm.id) ON CONFLICT DO NOTHING;
    END LOOP;

    -- finance: 用户查看 + 项目查看 + 采购申请全部 + 采购管理全部 + 任务全部
    FOR perm IN SELECT id FROM t_permission WHERE code IN ('user:read', 'project:read', 'pr:read', 'pr:create', 'pr:approve', 'task:read', 'task:complete', 'purchase:read', 'purchase:pending:read', 'purchase:purchased:read', 'purchase:arrived:read') LOOP
        INSERT INTO t_role_permission (role_id, permission_id) VALUES (finance_id, perm.id) ON CONFLICT DO NOTHING;
    END LOOP;

    -- user: 只读权限（含角色查看）
    FOR perm IN SELECT id FROM t_permission WHERE code IN ('user:read', 'role:read', 'project:read', 'pr:read', 'task:read') LOOP
        INSERT INTO t_role_permission (role_id, permission_id) VALUES (user_id, perm.id) ON CONFLICT DO NOTHING;
    END LOOP;

    -- temp_worker: 最小权限（查看自身 + 打卡）
    FOR perm IN SELECT id FROM t_permission WHERE code IN ('user:read', 'attendance:clock-in', 'attendance:clock-out') LOOP
        INSERT INTO t_role_permission (role_id, permission_id) VALUES (
            (SELECT id FROM t_role WHERE code = 'temp_worker'), perm.id
        ) ON CONFLICT DO NOTHING;
    END LOOP;

    -- supplier: 注册权限 + 自身资料管理
    FOR perm IN SELECT id FROM t_permission WHERE code IN ('supplier:register', 'supplier:profile') LOOP
        INSERT INTO t_role_permission (role_id, permission_id) VALUES (
            (SELECT id FROM t_role WHERE code = 'supplier'), perm.id
        ) ON CONFLICT DO NOTHING;
    END LOOP;

    -- 用户-角色关联：给已有的 admin 用户分配管理员角色
    INSERT INTO t_user_role (user_id, role_id)
    SELECT u.id, admin_id
    FROM t_user u
    WHERE u.username = 'admin'
      AND NOT EXISTS (SELECT 1 FROM t_user_role ur WHERE ur.user_id = u.id AND ur.role_id = admin_id);
END $$;

-- 修正序列值，使其与表中已有数据一致，避免 INSERT 时主键冲突
SELECT setval('t_user_id_seq', COALESCE((SELECT MAX(id) FROM t_user), 1));
SELECT setval('t_role_id_seq', COALESCE((SELECT MAX(id) FROM t_role), 1));
SELECT setval('t_permission_id_seq', COALESCE((SELECT MAX(id) FROM t_permission), 1));
SELECT setval('t_warehouse_id_seq', COALESCE((SELECT MAX(id) FROM t_warehouse), 1));
SELECT setval('t_material_id_seq', COALESCE((SELECT MAX(id) FROM t_material), 1));
SELECT setval('t_stock_balance_id_seq', COALESCE((SELECT MAX(id) FROM t_stock_balance), 1));
SELECT setval('t_stock_record_id_seq', COALESCE((SELECT MAX(id) FROM t_stock_record), 1));
SELECT setval('t_temp_worker_id_seq', COALESCE((SELECT MAX(id) FROM t_temp_worker), 1));
SELECT setval('t_attendance_id_seq', COALESCE((SELECT MAX(id) FROM t_attendance), 1));
SELECT setval('t_upload_file_id_seq', COALESCE((SELECT MAX(id) FROM t_upload_file), 1));
SELECT setval('t_construction_waste_id_seq', COALESCE((SELECT MAX(id) FROM t_construction_waste), 1));
SELECT setval('t_user_wechat_id_seq', COALESCE((SELECT MAX(id) FROM t_user_wechat), 1));
SELECT setval('t_attendance_area_id_seq', COALESCE((SELECT MAX(id) FROM t_attendance_area), 1));
SELECT setval('t_purchase_order_id_seq', COALESCE((SELECT MAX(id) FROM t_purchase_order), 1));
SELECT setval('t_supplier_id_seq', COALESCE((SELECT MAX(id) FROM t_supplier), 1));
