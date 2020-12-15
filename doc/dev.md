bookmartian development
=======================

A development container is defined in .devcontainer/Dockerfile that contains the build environments for java and node required to contribute to the bookmartian project. In addition, the devcontainer.json file includes the VS Code Remote Container configuration required to attach directly to the running container for editing and debugging.

Within the container
--------------------

- Java / Maven
    - code can be compiled with maven
    - go script is provided for convenience to launch the backend
    - backend will run on port 4567 by default

``` bash
./go -d /workspaces/bookmartian/data/
```

- Node / Vue
    - hot-reload development env can be launched directly from the /src/vue directory
    - front end will run on port 8080 by default

```bash
npm run serve
```

Building the docker container
-----------------------------

The development container does not have docker installed so docker builds are done elsewhere (host machine or in GitHub Actions).

```powershell
. .\scripts\build\build-docker.ps1
. .\go-docker.ps1
```
