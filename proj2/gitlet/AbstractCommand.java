package gitlet;

import java.nio.file.Path;

public abstract class AbstractCommand implements Command {
    protected final Repository repo;
    protected final WorkSpaceManager workSpaceManager;
    protected final BlobManager blobManager;
    protected final CommitManager commitManager;
    protected final String commitDirName;
    protected final String blobDirName;
    protected final StageManager stageManager;
    protected final BranchManager branchManager;
    protected final RemoteManager remoteManager;
    protected final Path CWD;
    protected final Path GITLET_DIR;
    protected final Path BLOB_DIR;
    protected final Path COMMIT_DIR;
    protected final Path STAGE_DIR;

    public AbstractCommand(Repository repo) {
        this.repo = repo;
        workSpaceManager = repo.getWorkSpaceManager();
        blobManager = repo.getBlobManager();
        commitManager = repo.getCommitManager();
        stageManager = repo.getStageManager();
        branchManager = repo.getBranchManager();
        remoteManager = repo.getRemoteManager();
        this.CWD = workSpaceManager.getCWDPath();
        GITLET_DIR = repo.getGitletPath();
        BLOB_DIR = blobManager.getBlobPath();
        COMMIT_DIR = commitManager.getCommitsPath();
        STAGE_DIR = stageManager.getStagePath();
        blobDirName = blobManager.getBlobDirName();
        commitDirName = commitManager.getCommitDirName();
    }
}
