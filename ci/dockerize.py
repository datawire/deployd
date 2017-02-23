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
import sys

from subprocess import call, Popen, PIPE, STDOUT

def check_semver(text):
    import semantic_version
    return semantic_version.Version(text)


def cmd(args):
    cmd = shlex.split(args)

    process = Popen(cmd, stdout=PIPE, stderr=STDOUT)
    lines = []
    while True:
        output = process.stdout.readline()
        if output == '' and process.poll() is not None:
            break
        if output:
            line = output.strip()
            print("=> " + line)
            lines.append(line)

    rc = process.poll()
    return rc, '\n'.join(lines)


def docker(args):
    cmd_args = "docker {}".format(args)
    (rc, out) = cmd(cmd_args)
    if rc != 0:
        raise ValueError("""Error! The `docker` command failed (exit code: {})

The command was: {}
        """.format(rc, cmd_args))


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
    branch = os.getenv('TRAVIS_BRANCH', 'none').replace('/', '_')
    docker_repo = os.getenv('DOCKER_REPO')
    commit = os.getenv('COMMIT', os.getenv('TRAVIS_COMMIT'))
    version = gradle('--quiet version')
    is_tag = os.getenv('TRAVIS_TAG') not in ['', None]
    is_pull_request = os.getenv('TRAVIS_PULL_REQUEST', 'false') != 'false'
    pull_request_number = os.getenv('TRAVIS_PULL_REQUEST_NUMBER')
    build_number = os.getenv('TRAVIS_BUILD_NUMBER')

    if is_pull_request:
        print("==> Triggered from GitHub PR <{}>: Docker image WILL NOT be built".format(pull_request_number))
    elif is_tag:
        handle_git_tag(docker_repo, commit, version)
    else:
        print("==> Triggered from Push <{}>: Docker image WILL be built".format(branch))
        docker('build -t {0}:{1} -t {0}:{2} -t {0}:ci-{3} --build-arg IMPL_VERSION={2} .'.format(docker_repo,
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
