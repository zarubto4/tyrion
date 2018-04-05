import sys
import requests

if len(sys.argv) < 3:
    print 'Missing arguments, correct syntax: download_release_asset.py <release_url> <access_token>'
    sys.exit(1)

headers = {'Authorization': 'token ' + sys.argv[2]}
response = requests.get(sys.argv[1], headers = headers)

if response.status_code != 200:
     print 'Request was unsuccessful'
     print response.text
     sys.exit(1)

release = response.json()

if len(release['assets']) == 0:
     print 'Release does not contain any asset'
     sys.exit(1)

for index, asset in enumerate(release['assets']):
     if asset['name'] == 'dist.zip':
         asset_url = asset['url']
         break
     elif index == len(release['assets']) - 1:
         print 'Cannot found any asset named dist.zip'
         sys.exit(1)

headers2 = {'Authorization': 'token ' + config['api_key'], 'Accept': 'application/octet-stream'}
package = requests.get(asset_url, headers = headers2)

with open('dist.zip', 'w') as fd:
     for chunk in package.iter_content(chunk_size=128):
         fd.write(chunk)

sys.exit(0)
