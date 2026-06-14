package cn.tx.aiagent.supplier.domain;

import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 供应商领域实体。
 * 使用私有构造器，通过静态工厂方法创建实例。
 */
@Getter
public class Supplier {

    private Long id;
    private Long userId;
    private String companyName;
    private String licenseNo;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private SupplierStatus status;
    private String remark;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Supplier() {}

    /** 创建新供应商 */
    public static Supplier create(Long userId, String companyName, String licenseNo,
                                  String contactPerson, String contactPhone, String address) {
        Supplier s = new Supplier();
        s.userId = userId;
        s.companyName = companyName;
        s.licenseNo = licenseNo;
        s.contactPerson = contactPerson;
        s.contactPhone = contactPhone;
        s.address = address;
        s.status = SupplierStatus.ACTIVE;
        s.isDeleted = 0;
        return s;
    }

    /** 从持久层还原领域实体 */
    public static Supplier fromPersistence(Long id, Long userId, String companyName,
                                           String licenseNo, String contactPerson, String contactPhone,
                                           String address, int statusCode, String remark,
                                           int isDeleted, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Supplier s = new Supplier();
        s.id = id;
        s.userId = userId;
        s.companyName = companyName;
        s.licenseNo = licenseNo;
        s.contactPerson = contactPerson;
        s.contactPhone = contactPhone;
        s.address = address;
        s.status = SupplierStatus.fromCode(statusCode);
        s.remark = remark;
        s.isDeleted = isDeleted;
        s.createdAt = createdAt;
        s.updatedAt = updatedAt;
        return s;
    }

    /** 更新供应商信息 */
    public void update(String companyName, String licenseNo, String contactPerson,
                       String contactPhone, String address, String remark) {
        if (companyName != null) this.companyName = companyName;
        if (licenseNo != null) this.licenseNo = licenseNo;
        if (contactPerson != null) this.contactPerson = contactPerson;
        if (contactPhone != null) this.contactPhone = contactPhone;
        if (address != null) this.address = address;
        if (remark != null) this.remark = remark;
    }

    /** 更新自身资料（不允许修改公司名称和统一社会信用代码） */
    public void updateProfile(String contactPerson, String contactPhone, String address) {
        if (contactPerson != null) this.contactPerson = contactPerson;
        if (contactPhone != null) this.contactPhone = contactPhone;
        if (address != null) this.address = address;
    }

    /** 变更状态 */
    public void changeStatus(SupplierStatus target) {
        this.status = target;
    }

    /** 设置 ID（MyBatis insert 后回填） */
    public void setId(Long id) {
        this.id = id;
    }

    /** 软删除 */
    public void markDeleted() {
        this.isDeleted = 1;
    }

    /** 是否已删除 */
    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }

    /** 是否正常 */
    public boolean isActive() {
        return status == SupplierStatus.ACTIVE;
    }
}
