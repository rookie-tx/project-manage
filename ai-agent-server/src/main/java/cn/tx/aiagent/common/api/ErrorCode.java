package cn.tx.aiagent.common.api;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum ErrorCode {

    SUCCESS(0, "result.success"),

    // 参数/校验错误 1xxx
    PARAM_MISSING(1001, "param.missing"),
    PARAM_INVALID(1002, "param.invalid"),
    PARAM_TYPE_ERROR(1003, "param.type_error"),

    // 认证/授权错误 2xxx
    NOT_AUTHENTICATED(2001, "auth.not_authenticated"),
    CREDENTIALS_INVALID(2002, "auth.credentials_invalid"),
    ACCOUNT_DISABLED(2003, "auth.account_disabled"),
    SESSION_EXPIRED(2004, "auth.session_expired"),
    ACCESS_DENIED(2005, "auth.access_denied"),

    // 项目管理错误 3xxx
    PROJECT_NOT_FOUND(3001, "project.not_found"),
    PROJECT_STATUS_TRANSITION_INVALID(3002, "project.status_transition_invalid"),
    PROJECT_MANAGER_NOT_FOUND(3003, "project.manager_not_found"),
    PROJECT_CODE_GENERATION_FAILED(3004, "project.code_generation_failed"),

    // 采购申请错误 4xxx
    PR_PROJECT_NOT_FOUND(4001, "pr.project_not_found"),
    PR_NOT_FOUND(4002, "pr.not_found"),
    PR_STATUS_TRANSITION_INVALID(4003, "pr.status_transition_invalid"),
    // 用户管理错误 7xxx
    USER_NOT_FOUND(7001, "user.not_found"),
    USERNAME_DUPLICATE(7002, "user.username_duplicate"),
    USER_DELETED(7003, "user.deleted"),
    PASSWORD_SAME_AS_OLD(7004, "user.password_same_as_old"),
    OLD_PASSWORD_INCORRECT(7005, "user.old_password_incorrect"),
    ROLE_CODE_DUPLICATE(7006, "role.code_duplicate"),
    ROLE_PROTECTED(7007, "role.protected"),
    ROLE_NOT_FOUND(7008, "role.not_found"),
    ROLE_HAS_USERS(7009, "role.has_users"),
    STOCK_INSUFFICIENT(7010, "stock.insufficient"),
    WAREHOUSE_NOT_FOUND(7011, "warehouse.not_found"),
    MATERIAL_NOT_FOUND(7012, "material.not_found"),
    STOCK_RECORD_NOT_FOUND(7013, "stock_record.not_found"),
    TEMP_WORKER_NOT_FOUND(7014, "temp_worker.not_found"),
    MUST_CHANGE_PASSWORD(7015, "auth.must_change_password"),
    ALREADY_CLOCKED_IN(7016, "attendance.already_clocked_in"),
    NOT_CLOCKED_IN(7017, "attendance.not_clocked_in"),
    ALREADY_CLOCKED_OUT(7018, "attendance.already_clocked_out"),
    OUT_OF_CHECKIN_RANGE(7019, "attendance.out_of_checkin_range"),
    GPS_ACQUISITION_FAILED(7020, "attendance.gps_acquisition_failed"),
    ATTENDANCE_AREA_NOT_FOUND(7021, "attendance.area_not_found"),

    // 文件上传错误 8xxx
    FILE_TOO_LARGE(8001, "file.too_large"),
    FILE_TYPE_NOT_ALLOWED(8002, "file.type_not_allowed"),
    FILE_NOT_FOUND(8003, "file.not_found"),

    // 建筑废渣管理错误 9xxx
    WASTE_PHOTO_REQUIRED(9001, "waste.photo_required"),
    WASTE_NOT_FOUND(9002, "waste.not_found"),

    // 微信认证错误 10xxx
    WECHAT_CODE_INVALID(10001, "wechat.code_invalid"),
    WECHAT_API_ERROR(10002, "wechat.api_error"),
    WECHAT_ALREADY_BOUND(10003, "wechat.already_bound"),
    WECHAT_NOT_BOUND(10004, "wechat.not_bound"),

    // 工作流/任务错误 6xxx
    TASK_NOT_FOUND(6001, "task.not_found"),
    TASK_NOT_OWNER(6002, "task.not_owner"),
    TASK_CALLBACK_NOT_FOUND(6003, "task.callback_not_found"),
    TASK_PROCESS_ALREADY_STARTED(6004, "task.process_already_started"),
    TASK_PROCESS_START_FAILED(6005, "task.process_start_failed"),

    // 系统/服务器错误 5xxx
    INTERNAL_ERROR(5001, "system.internal_error"),
    DATABASE_ERROR(5002, "system.database_error"),
    REMOTE_CALL_ERROR(5003, "system.remote_call_error"),

    // 未知错误
    UNKNOWN_ERROR(9999, "system.unknown_error");

    private final int code;

    private final String messageKey;

    ErrorCode(int code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }
}
