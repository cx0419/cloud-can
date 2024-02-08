package ccw.serviceinnovation.node.bo;

import ccw.serviceinnvation.encryption.consant.EncryptionEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class ObjectMeta implements Serializable {
    private static final long serialVersionUID = -6597003954824547294L;
    /**
     * hash值
     */
    private String etag;

    /**
     * 加密算法
     */
    private EncryptionEnum secret;

    /**
     * 纠删码参数
     */
    private RsParam rs;

    public ObjectMeta(String etag, EncryptionEnum secret) {
        this.etag = etag;
        this.secret = secret;
        this.rs = RsParam.instance;
    }
}
