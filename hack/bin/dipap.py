#!/usr/bin/env python

"""dipap.py

Development Integration Package and Push ("dipap"). Converts a directory into a GZIP compressed tarball and feeds it to
the development integration endpoint ("/integrations/dev")

Usage:
  dipap.py package <service_dir>
  dipap.py push    <service_pkg>
  
Options:
  -h --help     Show this screen.
  --version     Show version.
"""
import os


def package(source_dir):
    import tarfile
    import yaml
    
    with open('{0}/deployd.yaml'.format(source_dir), 'r') as f:
        deployd = yaml.load(f)
    
    with tarfile.open('{0}.tar.gz'.format(deployd['service']['name']), 'w:gz') as tar:
        tar.add(source_dir, arcname=os.path.basename(source_dir))
    

def push(service_file, push_endpoint):
    import requests
    
    data = open(service_file, 'rb').read()
    res = requests.post(url=push_endpoint,
                        data=data,
                        headers={'Content-Type': 'application/octet-stream'})

def main(args):
    if args['package']:
        package(args['<service_dir>'])
    elif args['push']:
        push(args['<service_pkg>'], 'http://localhost:8080/integrations/develop')

if __name__ == "__main__":
    from docopt import docopt

    main(docopt(__doc__))
