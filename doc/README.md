
# How to build the docs

First time, do this: Go to `doc` directory. Create virtual environment:
```
virtualenv env
source ./env/bin/activate
pip install -r requirements.txt
```
When this is done (once and for all) you can activate the virtual
environment just by running
```
source ./env/bin/activate
```
Once activated, you can build documentation with:
```
make html
```
