package com.exaltTraining;


//Helper class used to specify the form of returning company
public class companyPrinted {
    private int companyId;
    private String companyName;
    private String companyEmail;
    private Boolean isApproved;

    public companyPrinted(int companyId, String companyName, String companyEmail) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyEmail = companyEmail;
    }

    public companyPrinted(int companyId, String companyName, String companyEmail, Boolean isApproved) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyEmail = companyEmail;
        this.isApproved = isApproved;
    }

    public Boolean getApproved() {
        return isApproved;
    }

    public void setApproved(Boolean approved) {
        isApproved = approved;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }
}
