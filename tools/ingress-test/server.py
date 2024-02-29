from flask import Flask, make_response
import time

app = Flask(__name__)

@app.route('/test/<int:n>', methods=['GET'])
def test(n):
    time.sleep(n)
    return make_response('OK', 200)

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000, threaded=True)