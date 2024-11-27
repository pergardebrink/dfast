# Simple producer to send data

This will send csv data as json payload to the simple ingestion api

### Instructions
```
pip install poetry
poetry install
```

Run the commands with poetry (or activate the virtual environment)
```
poetry run python app.py ..\..\data\inventory.csv merchant1 inventory 
poetry run python app.py ..\..\data\orders.csv merchant1 order 
```
