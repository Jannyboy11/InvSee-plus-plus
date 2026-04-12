import requests
import os
import zipfile
import yaml

# TODO port https://gist.github.com/kennytv/a227d82249f54e0ad35005330256fee2

filePaths = {
    "InvSee++": "artifacts/InvSee++.jar",
    "InvSee++_Give": "artifacts/InvSee++_Give.jar",
    "InvSee++_Clear": "artifacts/InvSee++_Clear.jar"
}

def postVersion(headers: dict) -> dict:
    version = readPluginVersion(filePaths["InvSee++"])
    minecraftVersions = readMinecraftVersions()
    versionUpload = {
        # TODO change to "Release"
        "channel": "Alpha",
        "description": "InvSee++ release" + version,
        "files": [
            # TODO externalUrl only required for linking that version to another url?
            # TODO but we are just uploading the files directly, so we shouldn't use the externalUrl.
            {
                # "externalUrl": "TODO ???",
                "platforms": "[PAPER]"
            },
            {
                "platforms": "[PAPER]"
            },
            {
                "platforms": "[PAPER]"
            }
        ],
        "platformDependencies": {
            "PAPER": minecraftVersions
        },
        "version": version
    }
    files = {}
    for (fileName, filePath) in filePaths:
        with open(filePath, "rb") as f:
            files[fileName] = f

    response = requests.post(url, headers=headers, files=files, data=versionUpload)


def readPluginVersion(pluginFilePath: str) -> str:
    with zipfile.ZipFile(pluginFilePath, "r") as z:
        with z.open("plugin.yml") as file:
            data = yaml.safe_load(file)
            return data['version']

def headers(jwt: str | None = None) -> dict:
    headers = {
        "User-Agent": "InvSee++ automated deployment client"
    }
    if jwt != None:
        headers['Authorization'] = 'HangarAuth ' + jwt
    return headers

def fetchJwt(apiKey: str) -> str:
    url = "https://hangar.papermc.io/api/v1/authenticate"
    params = { "apiKey": apiKey }
    response = requests.post(url, headers=headers(), params=params)
    return response.json()

def readMinecraftVersions() -> list[str]:
    versions = os.environ['MINECRAFT_VERSIONS']
    return versions.split(' ')

def main():
    apiKey = os.environ['HANGAR_TOKEN']
    jwt = fetchJwt(apiKey)

if __name__ == '__main__':
    main()