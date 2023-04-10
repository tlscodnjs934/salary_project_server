package blankspace.blankspaceprj.dto;

import io.swagger.annotations.ApiModelProperty;

public class ResultDTO {

    @ApiModelProperty(example = "상태코드")
    private String resultCode;

    @ApiModelProperty(example = "상태 메세지")
    private String resultMsg;

    @ApiModelProperty(example = "데이터")
    private Object data;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}