# Github User Lookup

Simple API to acquire users repositories, that are not fork, with branches and last commit SHA by given username

## Requirements

- JDK 17

## Usage

### Start the server

```
./gradlew bootRun
```

### Run the tests

```
./gradlew test
```

### Request for username

```
GET localhost:8080/user/{user_name}
```

**Example response:**

```
[
    {
        "repositoryName": "Test",
        "ownerLogin": "maros2710",
        "branches": [
            {
                "name": "master",
                "lastCommitSha": "8967564f49b8b48a2b88312c85b6d55b0ba7bbca"
            },
            {
                "name": "master2",
                "lastCommitSha": "6bfb7957a16742edcc8adde192f76ba25584754e"
            }
        ]
    }
]
```

### Tips

GitHub public API allows only 60 requests per hour
You can increase this number to 5000 per hour by adding your personal API key to application.properties
You can get your personal API key [here](https://github.com/settings/tokens)

### Optimization

User can have multiple repositories with multiple branches. Repositories and branches are
loaded asynchronously, but if user has hundreds of repositories it can take some time.
There is cache to cache requests so when user sends request for specific user, second request
is caches for configurable time in application.properties.
[This](https://github.com/c9s) user is good for testing, since he has more than 4 hundreds repositories:
