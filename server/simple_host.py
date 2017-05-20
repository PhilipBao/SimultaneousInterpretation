# flask host for simultan translation

from __future__ import print_function
import flask
from flask import Flask, jsonify, request
from flask_restful import Resource, Api
import json
import urllib2, urllib
import sys
from flask_httpauth import HTTPBasicAuth

# API registered key on translate.yandex.net
API_KEY = "$$$"

auth = HTTPBasicAuth()
app = Flask(__name__)

# temp storage of conversation
conversation = {}
user_lang = {}


def eprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)

# Simple auth process
@auth.get_password
def get_password(username):
    if username == 'test001':
        return 'pwd'
    return None

# get api
@app.route('/api/v1.0/synctrans/<string:sender_id>', methods=['GET'])
@app.route('/api/v1.0/synctrans/<string:sender_id>/<string:lang>', methods=['GET'])
@auth.login_required
def get_recent_words(sender_id, lang = None):
    if sender_id not in conversation or len(conversation[sender_id]) is 0:
        return jsonify({'message': ''}), 200
    if not lang:
        return jsonify({'message': conversation[sender_id].pop()}), 200
    orig_msg = conversation[sender_id].pop()
    sender_lang = user_lang[sender_id]
    reciver_lang = lang
    lang_dir = sender_lang + "-" + reciver_lang
    request_url = "key=" + API_KEY + "&text=" + urllib.quote(orig_msg) + "&lang=" + urllib.quote(lang_dir)
    url = "https://translate.yandex.net/api/v1.5/tr.json/translate?" + request_url

    res = urllib2.urlopen(url).read()
    json_res = json.loads(res)
    return jsonify({'message':json_res['text'][0]}), 200

# post api
@app.route('/api/v1.0/synctrans/conversation', methods=['POST'])
@auth.login_required
def start_conversation():
    if not request.json or not 'sender_id' in request.json:
        flask.abort(400)

    sender_id = request.json['sender_id']
    msg = request.json['message']
    lang = 'en'
    if 'lang' in request.json:
        lang = request.json['lang']
    if sender_id not in conversation:
        conversation[sender_id] = []
        user_lang[sender_id] = lang
    if msg is not "":
        conversation[sender_id].insert(0, msg)

    return jsonify({'sender_id': sender_id, 'message': msg}), 201

@app.errorhandler(404)
def not_found(error):
    return flask.make_response(jsonify({'error': 'Not found'}), 404)

@auth.error_handler
def unauthorized():
    return flask.make_response(jsonify({'error': 'Unauthorized access'}), 401)

if __name__ == '__main__':
    app.run()
