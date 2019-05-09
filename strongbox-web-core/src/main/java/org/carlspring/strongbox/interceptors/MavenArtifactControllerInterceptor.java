package org.carlspring.strongbox.interceptors;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;
import static org.carlspring.strongbox.controllers.layout.maven.MavenArtifactController.HEADER_MAVEN_USER_AGENT_VALUE;
import static org.springframework.http.HttpHeaders.USER_AGENT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * @author Przemyslaw Fusik
 */
public class MavenArtifactControllerInterceptor
        extends BaseArtifactControllerInterceptor
{

    @Autowired
    protected RepositoryPathResolver repositoryPathResolver;

    @Autowired
    protected ConfigurationManagementService configurationManagementService;

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler)
            throws IOException
    {
        if (!HEADER_MAVEN_USER_AGENT_VALUE.equals(request.getHeader(USER_AGENT)))
        {
            return true;
        }

        final Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables == null)
        {
            return true;
        }

        final String storageId = (String) pathVariables.get("storageId");
        final String repositoryId = (String) pathVariables.get("repositoryId");

        if (StringUtils.isBlank(storageId) || StringUtils.isBlank(repositoryId))
        {
            return true;
        }

        final String path = (String) pathVariables.get("path");

        final Storage storage = getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);
        if (StringUtils.isBlank(path))
        {
            response.sendError(BAD_REQUEST.value(), "Path should be provided!");
            return false;
        }
        if (path.endsWith("/"))
        {
            response.sendError(BAD_REQUEST.value(), "The specified path should not ends with `/` character!");
            return false;
        }
        final RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, path);
        final String method = request.getMethod();
        final boolean gavRequiredHttpMethod = isGavRequiredHttpMethod(method);
        if (Files.exists(repositoryPath) && Files.isDirectory(repositoryPath) && gavRequiredHttpMethod)
        {
            response.sendError(BAD_REQUEST.value(), "The specified path is a directory!");
            return false;
        }

        MavenArtifact mavenArtifact = MavenArtifactUtils.convertPathToArtifact(repositoryPath);
        if ((StringUtils.isBlank(mavenArtifact.getArtifactId()) ||
             StringUtils.isBlank(mavenArtifact.getGroupId()) ||
             StringUtils.isBlank(mavenArtifact.getVersion())) && (gavRequiredHttpMethod))
        {
            response.sendError(BAD_REQUEST.value(), "The specified path is not a Maven GAV request!");
            return false;
        }

        return true;
    }

    private static boolean isGavRequiredHttpMethod(String method)
    {
        return "get".equals(method) || "head".equals(method) || "post".equals(method) || "put".equals(method) ||
               "patch".equals(method);
    }

}
