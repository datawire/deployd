#!/usr/bin/env bash
set -euo pipefail

/usr/sbin/nginx && uwsgi --ini /service/config/uwsgi.ini
