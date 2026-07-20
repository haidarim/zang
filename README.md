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


