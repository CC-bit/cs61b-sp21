package gitlet;

import java.nio.file.Path;

public abstract class AbstractCommand implements Command {
    protected final Repository repo;
    protected final WorkSpaceManager workSpaceManager;
    protected final BlobManager blobManager;
    protected final CommitManager commitManager;
    protected final StageManager stageManager;
    protected final BranchManager branchManager;
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
        CWD = workSpaceManager.getCWDPath();
        GITLET_DIR = repo.getGitletPath();
        BLOB_DIR = blobManager.getBlobPath();
        COMMIT_DIR = commitManager.getCommitsPath();
        STAGE_DIR = stageManager.getStagePath();
    }
}
