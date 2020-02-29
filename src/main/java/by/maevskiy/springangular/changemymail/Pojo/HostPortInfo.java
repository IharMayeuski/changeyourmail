package by.maevskiy.springangular.changemymail.Pojo;

import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class HostPortInfo {
    public String host;
    public String sslTrust;
    public String port;
}
