(ns ow.logging.api.alpha.js
  (:require-macros [ow.logging.api.alpha :as am])
  (:require [ow.logging.api.alpha :as a]))

(defn- stringify [v]
  (cond
    (str? v) v
    true     (.stringify js/JSON v)))

(defn ^:export with-checkpoint [name cb]
  (am/with-checkpoint name
    (cb)))

(defn ^:export with-data [data cb]
  (am/with-data (stringify data)
    (cb)))

(defn ^:export get-checkpoints []
  (clj->js (a/get-checkpoints)))

(defn ^:export get-root-checkpoint []
  (clj->js (a/get-root-checkpoint)))

(defn ^:export log-data [level msg & [data]]
  (clj->js (a/log-data (keyword level) msg (stringify data))))

(defn ^:export log [level msg & [data]]
  (a/log (keyword level) msg (stringify data)))

(defn ^:export trace [msg & [data]]
  (a/trace msg (stringify data)))

(defn ^:export debug [msg & [data]]
  (a/debug msg (stringify data)))

(defn ^:export info [msg & [data]]
  (a/info msg (stringify data)))

(defn ^:export warn [msg & [data]]
  (a/warn msg (stringify data)))

(defn ^:export error [msg & [data]]
  (a/error msg (stringify data)))

(defn ^:export fatal [msg & [data]]
  (a/fatal msg (stringify data)))

(defn ^:export attach [obj]
  (a/attach obj))

(defn ^:export detach [obj]
  (a/detach obj))

(defn ^:export with-historical-logging-info [logging-info cb]
  (am/with-historical-logging-info logging-info
    (cb)))

(defn ^:export with-detached-logging-info [obj cb]
  (am/with-detached-logging-info obj
    (cb)))
