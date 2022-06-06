from fastapi import FastAPI
import os

app = FastAPI()


@app.get("/")
async def root():
    return {"Hello": "World"}


@app.get("/another")
async def another():
    return {"path": os.getcwd()}
