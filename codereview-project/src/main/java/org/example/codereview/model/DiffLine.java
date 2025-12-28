package org.example.codereview.model;

public class DiffLine {
    private DiffType type;
    
    private Integer leftLineNumber;
    private Integer rightLineNumber;
    
    private String leftText;
    private String rightText;
    
    public DiffLine() {
    }
    
    public DiffType getType() {
        return type;
    }
    
    public void setType(DiffType type) {
        this.type = type;
    }
    
    public Integer getLeftLineNumber() {
        return leftLineNumber;
    }
    
    public void setLeftLineNumber(Integer leftLineNumber) {
        this.leftLineNumber = leftLineNumber;
    }
    
    public Integer getRightLineNumber() {
        return rightLineNumber;
    }
    
    public void setRightLineNumber(Integer rightLineNumber) {
        this.rightLineNumber = rightLineNumber;
    }
    
    public String getLeftText() {
        return leftText;
    }
    
    public void setLeftText(String leftText) {
        this.leftText = leftText;
    }
    
    public String getRightText() {
        return rightText;
    }
    
    public void setRightText(String rightText) {
        this.rightText = rightText;
    }
}

