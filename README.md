# github-sync

Synchronise labels between a source and target repository

```
Usage: github-sync --token <string> --source <repository> --target <repository> [--delete] [--dry-run] [--verbose]

synchronise labels between github repositories

Options and flags:
    --help
        Display this help text.
    --token <string>
        personal access token with permissions for source and target repositories
    --source <repository>
        source repository as owner/repo
    --target <repository>
        target repository as owner/repo
    --delete
        delete additional labels not found in source repository
    --dry-run
        only log actions to be taken
    --verbose
        show logging of http requests

```

Default behaviour will create labels in the target repository that are present in the source and update those that have matching names.
If `--delete` is passed, labels that are present in the target but not in the source will be removed.
`--dry-run` can be used to see what labels will be created, updated and deleted prior to executing properly.
