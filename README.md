# DistributedCacheAkka

## Running the project

There are two ways to run this project:

<br>

#### 1. Running all the nodes in single JVM

  ```bash
  cd <project-directory>
  ./start_on_single_jvm.sh 
  ```
<br>

#### 2. Running all the nodes in Separate JVMs
  
  ```bash
  cd <project-directory>
  ./start_on_separte_jvms.sh
  ```

<br>

## Testing Via Postman
Copy the below JSON into Postman to test the distributed cache functionality. This collection includes requests to PUT, GET, and DELETE cache entries on two different servers.
```json
{
  "info": {
    "_postman_id": "0293c63f-0a25-462b-9cb7-26327ab09d35",
    "name": "Distributed Cache",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    "_exporter_id": "37395741"
  },
  "item": [
    {
      "name": "DELETE Server 2",
      "request": {
        "auth": {
          "type": "noauth"
        },
        "method": "DELETE",
        "header": [],
        "url": {
          "raw": "http://localhost:12552/cache/key1",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "12552",
          "path": [
            "cache",
            "key1"
          ]
        }
      },
      "response": []
    },
    {
      "name": "DELETE Server 1",
      "request": {
        "auth": {
          "type": "noauth"
        },
        "method": "DELETE",
        "header": [],
        "url": {
          "raw": "http://localhost:12551/cache/key1",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "12551",
          "path": [
            "cache",
            "key1"
          ]
        }
      },
      "response": []
    },
    {
      "name": "PUT Server 2",
      "request": {
        "auth": {
          "type": "noauth"
        },
        "method": "PUT",
        "header": [],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"value\": \"123\"\n}",
          "options": {
            "raw": {
              "language": "json"
            }
          }
        },
        "url": {
          "raw": "http://localhost:12552/cache/key1",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "12552",
          "path": [
            "cache",
            "key1"
          ]
        }
      },
      "response": []
    },
    {
      "name": "PUT Server 1",
      "request": {
        "auth": {
          "type": "noauth"
        },
        "method": "PUT",
        "header": [],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"value\": \"12345\"\n}",
          "options": {
            "raw": {
              "language": "json"
            }
          }
        },
        "url": {
          "raw": "http://localhost:12551/cache/key1",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "12551",
          "path": [
            "cache",
            "key1"
          ]
        }
      },
      "response": []
    },
    {
      "name": "GET Server 2",
      "request": {
        "auth": {
          "type": "noauth"
        },
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:12552/cache/key1",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "12552",
          "path": [
            "cache",
            "key1"
          ]
        }
      },
      "response": []
    },
    {
      "name": "GET Server 1",
      "request": {
        "auth": {
          "type": "noauth"
        },
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:12551/cache/key1",
          "protocol": "http",
          "host": [
            "localhost"
          ],
          "port": "12551",
          "path": [
            "cache",
            "key1"
          ]
        }
      },
      "response": []
    }
  ]
}
```