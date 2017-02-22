#!/usr/bin/env python

import time

from flask import Flask, jsonify
app = Flask(__name__)


@app.route('/hello/<name>', methods=['GET'])
def hello(name):
    return jsonify(message="Hello, {}!".format(name), time=int(round(time.time() * 1000)))


@app.route('/health', methods=['GET', 'HEAD'])
def health():
    # TODO: Custom health check logic.
    return '', 200


def main():
    app.run(debug=True)


if __name__ == '__main__':
    main()
