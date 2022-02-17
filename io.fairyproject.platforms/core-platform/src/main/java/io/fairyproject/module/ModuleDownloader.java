package io.fairyproject.module;

import io.fairyproject.Debug;
import io.fairyproject.FairyPlatform;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class ModuleDownloader {

    public static final boolean LOCAL_REPO = Boolean.getBoolean("fairy.local-repo");
    public static final Path MODULE_DIR = new File(FairyPlatform.INSTANCE.getDataFolder(), "modules").toPath().toAbsolutePath();

    private static final RepositorySystem REPOSITORY;
    private static final DefaultRepositorySystemSession SESSION;
    private static final List<RemoteRepository> REPOSITORIES;

    static {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        REPOSITORY = locator.getService(RepositorySystem.class);
        SESSION = MavenRepositorySystemUtils.newSession();

        SESSION.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL);
        SESSION.setLocalRepositoryManager(REPOSITORY.newLocalRepositoryManager(SESSION, new LocalRepository(LOCAL_REPO ? locateMavenLocal() : MODULE_DIR.toFile())));
        SESSION.setReadOnly();
        REPOSITORIES = REPOSITORY.newResolutionRepositories(SESSION, Arrays.asList(
                new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2").build(),
                new RemoteRepository.Builder("imanityLibraries", "default", "https://maven.imanity.dev/repository/imanity-libraries").build()
        ));
    }

    private File locateMavenLocal() {
        String localOverride = System.getProperty("maven.repo.local");
        if (localOverride != null) {
            return new File(localOverride);
        } else {
            return (new File(System.getProperty("user.home"), "/.m2/repository")).getAbsoluteFile();
        }
    }

    @SuppressWarnings("Duplicates")
    public static Path download(String groupId, String artifactId, String version, @Nullable String customRepository) throws IOException {
        Files.createDirectories(MODULE_DIR);

        if (Debug.IN_FAIRY_IDE) {
            final File projectFolder = (Debug.UNIT_TEST ? Paths.get("").toAbsolutePath().getParent() : Paths.get("").toAbsolutePath().getParent().getParent()).toFile(); // double parent
            final File localRepoFolder = new File(projectFolder, "libs/local");

            if (!localRepoFolder.exists()) {
                Debug.logExceptionAndPause(new IllegalStateException("Couldn't found local repo setup at " + localRepoFolder + "!"));
            }

            File file = new File(localRepoFolder, groupId + "." + artifactId + ".jar");
            if (!file.exists()) {
                Debug.logExceptionAndPause(new IllegalStateException("Couldn't found local module at local repo setup at " + file + "!"));
            }
            return file.toPath();
        }

        DependencyResult result;
        try {
            List<RemoteRepository> repositories = ModuleDownloader.REPOSITORIES;
            if (customRepository != null) {
                repositories = REPOSITORY.newResolutionRepositories(SESSION, Arrays.asList(
                        new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2").build(),
                        new RemoteRepository.Builder("imanityLibraries", "default", "https://maven.imanity.dev/repository/imanity-libraries").build(),
                        new RemoteRepository.Builder("custom", "default", customRepository).build()
                ));
            }

            Artifact artifact = new DefaultArtifact(
                    groupId,
                    artifactId,
                    "jar",
                    version
            );
            final Dependency dependency = new Dependency(
                    artifact,
                    null
            );

            result = REPOSITORY.resolveDependencies(SESSION, new DependencyRequest(new CollectRequest((Dependency) null, Collections.singletonList(dependency), repositories), null));
        } catch (DependencyResolutionException ex) {
            Debug.logExceptionAndPause(new IllegalStateException("Error resolving libraries!", ex));
            return null;
        }

        final ArtifactResult artifactResult = result.getArtifactResults().get(0); // Assuming we only have 1 result
        return artifactResult.getArtifact().getFile().toPath();
    }

}
