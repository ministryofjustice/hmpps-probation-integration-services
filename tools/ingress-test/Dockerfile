FROM python:3-slim
WORKDIR /app

RUN useradd --uid 100 --create-home --system app
USER 100

RUN pip install --no-cache-dir Flask==3.0.2
COPY server.py server.py
CMD ["python", "server.py"]