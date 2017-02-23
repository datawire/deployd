#!/usr/bin/env python

"""docker_build.py

Usage:
    dockerize.py [options]

Options:
    -h --help       Show this screen.
    --no-push       Do NOT push image tags to a Docker repository.
    --version       The version of this program.

"""

import os
import shlex

from subprocess import call, Popen, PIPE, STDOUT

def check_semver(text):
    import semantic_version
    return semantic_version.Version(text)


def cmd(args):
    args = shlex.split(args)
    proc = Popen(args, stdout=PIPE, stderr=STDOUT)
    out, err = proc.communicate()
    exitcode = proc.returncode
    return exitcode, out, err


def docker(args):
    cmd("docker {}".format(args))


def gradle(args):
    return cmd("./gradlew {}".format(args))[1].rstrip()


def handle_git_tag(repo, commit, tag):
    if tag == 'latest':
        raise ValueError('Invalid tag name "latest" not allowed!')

    check_semver(tag)

    print("==> Triggered from Tag <{0}>: Pulling Docker image associated with the tagged commit and adding "
          + "additional tag for version (version: {0})".format(tag))

    docker("pull {}:{}".format(repo, commit))
    docker("tag  {0}:{1} {0}:{2}".format(repo, commit, tag))


def main(args):
    print(gradle('--quiet version'))
    branch = os.getenv('TRAVIS_BRANCH', 'none').replace('/', '_')
    docker_repo = os.getenv('DOCKER_REPO')
    commit = os.getenv('COMMIT', os.getenv('TRAVIS_COMMIT'))
    version = os.getenv('TRAVIS_TAG') or 'latest' if branch == 'master' else branch
    is_tag = os.getenv('TRAVIS_TAG') != ''
    is_pull_request = os.getenv('TRAVIS_PULL_REQUEST', 'false') != 'false'
    pull_request_number = os.getenv('TRAVIS_PULL_REQUEST_NUMBER')
    build_number = os.getenv('TRAVIS_BUILD_NUMBER')

    if is_pull_request:
        print("==> Triggered from GitHub PR <{}>: Docker image WILL NOT be built".format(pull_request_number))
    elif is_tag:
        handle_git_tag(docker_repo, commit, version)
    else:
        print("==> Triggered from Push <{}>: Docker image WILL be built".format(branch))
        docker('build -t {0}:{1} -t {0}:{2} -t {0}:ci-{3} --build-arg IMPL_VERSION=0.1.0 .'.format(docker_repo,
                                                                                                   commit,
                                                                                                   version,
                                                                                                   build_number))

    if args['--no-push']:
        print("==> Skipping Docker image push")
        return
    else:
        docker('push {}'.format(docker_repo))


if __name__ == '__main__':
    from docopt import docopt

    exit(main(docopt(__doc__)))
