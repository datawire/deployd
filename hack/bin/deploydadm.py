#!/usr/bin/env python

"""deploydadm.py

Temporary administrative controls for Deployd (install, reinstall, reconfigure etc.)

Usage:
  deploydadm.py install <configuration-file>
  deploydadm.py uninstall

Options:
  -h --help     Show this screen.
  --version     Show version.
"""

if __name__ == "__main__":
    from docopt import docopt

    main(docopt(__doc__))
