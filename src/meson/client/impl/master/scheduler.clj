(ns meson.client.impl.master.scheduler
  (:require [clj-http.conn-mgr :as http-conn-mgr]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [meson.http.core :as http]
            [meson.util.core :as util])
  (:import [clojure.lang Keyword]))

(def scheduler-path "api/v1/scheduler")
(def default-scheduler-timeout 10)
(def default-scheduler-threads 4)
(def default-scheduler-opts
  {:timeout default-scheduler-timeout
   :threads default-scheduler-threads})

(defn start
  ""
  ([this]
   (start this {}))
  ([this opts]
   (log/debug "Starting connection manager for the scheduler ...")
   (->> opts
        (into default-scheduler-opts)
        (http-conn-mgr/make-reusable-conn-manager)
        (assoc this :conn-mgr))))

(defn stop
  ""
  [this]
  (log/debug "Stopping connection manager for the scheduler ...")
  (http-conn-mgr/shutdown-manager (:conn-mgr this)))

(defn payload-key
  "Determine the payload's key.
   Some API endpoints may break the 1 to 1 pattern of :type
   and payload key."
  [^Keyword type]
  (condp = type
    :request (str (util/keyword->lower type) "s")
    (util/keyword->lower type)))

(comment
  (payload-key :request) ; plural expected
  (payload-key :accept) ; singular expected
  (payload-key :subscribe) ; singular expected
  (payload-key :acknowledge_operation_status))

(defn call
  ([this ^Keyword type]
   (call this type nil nil))
  ([this ^Keyword type payload framework-id]
   (call this type payload framework-id http/json-content-type))
  ([this ^Keyword type payload framework-id content-type]
   (call this type payload framework-id content-type {}))
  ([this ^Keyword type payload framework-id content-type opts]
   (let [data {:type (util/keyword->upper type)
               (payload-key type) payload}]
     (http/post
      this
      scheduler-path
      :body (json/write-str (merge data (or framework-id {})))
      :opts (into opts {:content-type content-type
                        :accept content-type})))))

(defn accept
  ([this payload stream-id framework-id]
   (accept this payload stream-id framework-id http/json-content-type))
  ([this payload stream-id framework-id content-type]
   (call
    this
    :accept
    payload
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn acknowledge
  ([this payload stream-id framework-id]
   (acknowledge this payload stream-id framework-id http/json-content-type))
  ([this payload stream-id framework-id content-type]
   (call
    this
    :acknowledge
    payload
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn acknowledge-operation-status
  ([this payload stream-id framework-id]
   (acknowledge-operation-status this payload stream-id framework-id http/json-content-type))
  ([this payload stream-id framework-id content-type]
   (call
    this
    :acknowledge_operation_status
    payload
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn decline
  ([this payload stream-id framework-id]
   (decline this payload stream-id framework-id http/json-content-type))
  ([this payload stream-id framework-id content-type]
   (call
    this
    :decline
    payload
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn kill-task
  ([this payload stream-id framework-id]
   (kill-task this payload stream-id framework-id http/json-content-type))
  ([this payload stream-id framework-id content-type]
   (call
    this
    :kill
    payload
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn message
  ([this payload stream-id framework-id]
   (message this payload stream-id framework-id http/json-content-type))
  ([this payload stream-id framework-id content-type]
   (call
    this
    :message
    payload
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn reconcile
  ([this payload stream-id framework-id]
   (reconcile this payload stream-id framework-id http/json-content-type))
  ([this payload stream-id framework-id content-type]
   (call
    this
    :reconcile
    payload
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn reconcile-operations
  ([this payload stream-id framework-id]
   (reconcile-operations this payload stream-id framework-id http/json-content-type))
  ([this payload stream-id framework-id content-type]
   (call
    this
    :reconcile_operations
    payload
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn request
  ([this payload stream-id framework-id]
   (request this payload stream-id framework-id http/json-content-type))
  ([this payload stream-id framework-id content-type]
   (call
    this
    :request  ; type is request, json keyword is requests (plural)
    payload
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn revive
  ([this payload stream-id framework-id]
   (revive this payload stream-id framework-id http/json-content-type))
  ([this payload stream-id framework-id content-type]
   (call
    this
    :revive
    payload
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn shutdown-executor
  ([this payload stream-id framework-id]
   (shutdown-executor this payload stream-id framework-id http/json-content-type))
  ([this payload stream-id framework-id content-type]
   (call
    this
    :shutdown
    payload
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn subscribe
  ([this payload]
   (subscribe this payload http/json-content-type))
  ([this payload content-type]
   (call
    this
    :subscribe
    payload
    nil
    content-type
    {:as :stream
     :streaming? true
     :chunked? true
     :connection http/keep-alive
     :connection-manager (:conn-mgr this)})))

(defn suppress
  ([this payload stream-id framework-id]
   (suppress this payload stream-id framework-id http/json-content-type))
  ([this payload stream-id framework-id content-type]
   (call
    this
    :suppress
    payload
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn teardown
  ([this stream-id framework-id]
   (teardown this stream-id framework-id http/json-content-type))
  ([this stream-id framework-id content-type]
   (call
    this
    :teardown
    nil
    framework-id
    content-type
    {:connection-manager (:conn-mgr this)
     :headers {:mesos-stream-id stream-id}})))

(defn update-framework
  ([this payload]
   (update-framework this payload http/json-content-type))
  ([this payload content-type]
   (call
    this
    :update_framework
    payload
    nil
    content-type
    {:as :stream
     :streaming? true
     :chunked? true
     :connection http/keep-alive
     :connection-manager (:conn-mgr this)})))

(def behaviour
  {:accept accept
   :acknowledge acknowledge
   :acknowledge-operation-status acknowledge-operation-status
   :decline decline
   :kill-task kill-task
   :message message
   :reconcile reconcile
   :reconcile-operations reconcile-operations
   :request request
   :revive revive
   :shutdown-executor shutdown-executor
   :subscribe subscribe
   :suppress suppress
   :teardown teardown
   :update-framework update-framework})
