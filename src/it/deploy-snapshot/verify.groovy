import org.carlspring.maven.commons.util.ArtifactUtils
import org.carlspring.strongbox.client.ArtifactClient


def artifact = ArtifactUtils.getArtifactFromGAV("org.carlspring.maven:test-project:1.0.8-SNAPSHOT");

def client = new ArtifactClient();

if (client.artifactExists(artifact, "storage0", "snapshots"))
{
    return true;
}

return false;
