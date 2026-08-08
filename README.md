## Domain 
```txt
[ USER SERVICE ]
  - CLIENT
  - PROFILE
  - SETTINGS

[ CHAT SERVICE ]
  - CONVERSATION
  - CHAT
  - PARTICIPANTS

[ NOTIFICATION SERVICE ]
  - NOTIFICATION

[ CALL SERVICE ]
  - CALL
  - CALL_PARTICIPANT

[ ANALYTICS SERVICE ]
  - LOGIN_HISTORY
  - AUDIT_LOG

```


**DB strategy:**
1. User DB (Sharded by CLIENT_ID)
CLIENT
PROFILE
SETTINGS
2. Chat DB (Sharded by CONVERSATION_ID)
CHAT
CONVERSATION_PARTICIPANT
3. Event System (Kafka)
message events
notification triggers
4. Cache Layer (Redis)
online status
last messages
unread counts


## Message Flow (Optimized)
User sends message
Write to CHAT (append-only)
Publish event → Kafka
Consumers:
update notifications
push via WebSocket
update unread counts

## Read Optimization
Inbox query (fast):
from Redis cache
fallback to DB


## Scaling Strategy
Layer	Scaling
-----------------
DB	| horizontal sharding
Chat |	partitioned writes
Queue |	Kafka
API	| stateless autoscaling
Cache |	Redis cluster


## SHARDING
```txt
                 API
                  |
        ---------------------
        |         |         |
        |         |         |
   User Router Chat Router Notification Router
        |         |         |
        |         |         |
  64 DBs     64 DBs     32 DBs

CLIENT_ID  CONVERSATION_ID CLIENT_ID
```


## PARTITIONING
growth issue: fix by either
	- partiioning 
	- bucketed
	- store messages in append-only log similar to kafka internally (best at scale)
		e.g. (conversation_id, offset, payload)




```txt
       [ USER CLIENT ]
        │          ▲
(1) Req │          │ (10) Resp: HTTPS
        ▼          │
 ┌────────────────────────┐
 │   1. API GATEWAY       │ <── Reverse Proxy #1 (Edge)
 └──────┬──────────▲──────┘
        │          │
(2) Req │          │ (9) Resp: HTTP/2 or gRPC
        ▼          │
 ┌────────────────────────┐
 │   2. K8S INGRESS       │ <── Reverse Proxy #2 (Cluster Entrance)
 └──────┬──────────▲──────┘
        │          │
(3) Req │          │ (8) Resp: Internal Cluster IP
        ▼          │
 ┌────────────────────────┐
 │   3. MICROSERVICE      │ <── Application Logic Pod
 └──────┬──────────▲──────┘
        │          │
(4) Req │          │ (7) Resp: DB Wire Protocol (gRPC (over HTTP/2))
        ▼          │
 ┌────────────────────────┐
 │   4. SHARD SERVICE     │ <── Database Routing Proxy
 └──────┬──────────▲──────┘
        │          │
(5) Req │          │ (6) Resp: Raw Data Rows (Native Database Wire Protocol (TCP)) 
        ▼          │
 ┌────────────────────────┐
 │   5. TARGET DB SHARD   │ <── Persistent Storage Node
 └────────────────────────┘
```



```txt
              +------------------+
              |  API Gateway     |  (Go)
              +------------------+
                        |
        -----------------------------------
        |                 |               |
        v                 v               v

  user-service     chat-service    notification
   (Java)            (Java)          (Java)

        |                 |               |
        ----------- gRPC ------------------
                        |
                        v
               +------------------+
               |  Shard Service   |  (Java)
               +------------------+
                        |
                        v
                Shard Metadata DB

                        |
                        v
          ------------------------------
          |            |               |
      shard-1      shard-2        shard-3
     (Postgres)   (Postgres)     (Postgres)
```


## Running the application 

```txt

                    Kubernetes Cluster (local)
                    (kind / minikube)

                         |
                         |
                  Spring Boot App
                         |
          +--------------+--------------+
          |                             |
   Metadata Database              Sharding Router
          |                             |
          |                 +-----------+-----------+
          |                 |           |           |
          v                 v           v           v

     metadata-db        shard-db-0  shard-db-1  shard-db-2
                            |           |           |
                            v           v           v
                         CLIENT      CLIENT      CLIENT
                         CHAT        CHAT        CHAT
                         CALL        CALL        CALL
```



POD: wraped container that  runs microservice + helper containers
has network IP, shared storage, lifecycle management. Pod is the env 

Node: server e.g. VM 


## K8n cluster 
1. Control Plane
Responsible for:
- storing the desired state
- scheduling workloads
- monitoring nodes
- restarting failed containers

2. Main components:
- API Server
- Scheduler
- Controller Manager
- etcd (database storing cluster state)

```txt
Kubernetes Cluster
│
├── Control Plane (the brain)
│
└── Worker Nodes (machines that run applications)
       │
       ├── Pods
       │    └── Containers
       │
       └── Pods
```


```txt
        Application ID
              |
              v
        Virtual Shard
              |
              v
        Physical Shard
              |
              v
        Database Node
```




```txt

orelease/
│
├── apps/
│   ├── web-app/
│   └── mobile-app/
│
├── backend/                    # Java ecosystem
│   ├── pom.xml                 # Maven reactor parent
│   │
│   ├── libs/
│   │   └── orelease-data/
│   │
│   ├── platform/
│   │   ├── config-service/
│   │   │   └── pom.xml
│   │   │
│   │   └── shard-service/
│   │       └── pom.xml
│   │
│   └── services/
│       ├── user-service/
│       │   └── pom.xml
│       │
│       ├── chat-service/
│       │   └── pom.xml
│       │
│       ├── notification-service/
│       │   └── pom.xml
│       │
│       └── analytics-service/
│           └── pom.xml
│
│
├── infrastructure/             # Go ecosystem
│   │
│   ├── go.mod
│   ├── go.sum
│   │
│   ├── api-gateway/
│   │   ├── cmd/
│   │   │   └── gateway/
│   │   │       └── main.go
│   │   │
│   │   ├── internal/
│   │   │   ├── middleware/
│   │   │   ├── routing/
│   │   │   ├── auth/
│   │   │   └── config/
│   │   │
│   │   └── Dockerfile
│   │
│   └── service-discovery/
│       ├── cmd/
│       │   └── discovery/
│       │       └── main.go
│       │
│       ├── internal/
│       │   ├── registry/
│       │   ├── health/
│       │   └── watcher/
│       │
│       └── Dockerfile
│
│
├── deployment/
│   ├── docker/
│   ├── helm/
│   └── k8s/
│
├── scripts/
│
├── README.md
└── application.sh
```


BIGINT	Long
BIGSERIAL	Long
SERIAL	Integer
INT	Integer
SMALLINT	Short
UUID	UUID





