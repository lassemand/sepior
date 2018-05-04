#!/bin/bash

gitlfs_version="1.5.2"

cache_directory="$HOME/cache"

# Install git-lfs

gitlfs_directory="${cache_directory}/git-lfs"
gitlfs_executable="${cache_directory}/git-lfs/git-lfs"
mkdir -p "${gitlfs_directory}"
if [ ! -f "${gitlfs_executable}" ]; then
  echo "git-lfs not found - downloading."
  cd "${gitlfs_directory}" || exit 1
  curl -sSL "https://github.com/git-lfs/git-lfs/releases/download/v${gitlfs_version}/git-lfs-linux-amd64-${gitlfs_version}.tar.gz" | tar xzf - --strip-components=1 "git-lfs-${gitlfs_version}/git-lfs"
else
  echo "git-lfs found."
fi
sudo cp "${gitlfs_executable}" /usr/local/bin/git-lfs
git lfs install
