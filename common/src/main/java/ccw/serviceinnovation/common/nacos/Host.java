package ccw.serviceinnovation.common.nacos;

import lombok.Data;

/**
 * @author 陈翔
 */
@Data
public class Host{
        @Data
        public static class Metadata{
                public String group;
                public Integer port;
                public String cold_storage_name;

        }
        private String ip;
        private Integer port;
        private String group;
        private Metadata metadata;
}