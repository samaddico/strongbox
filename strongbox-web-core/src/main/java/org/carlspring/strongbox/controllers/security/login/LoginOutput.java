package org.carlspring.strongbox.controllers.security.login;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class LoginOutput
{

    @XmlElement
    private String token;

    public LoginOutput(String token)
    {
        this.token = token;
    }

    public LoginOutput()
    {
    }
}
