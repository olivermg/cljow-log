(ns ow.logging.api.alpha.js
  (:require-macros [ow.logging.api.alpha :as am])
  (:require [ow.logging.api.alpha :as a]))

(defn ^:export with-checkpoint [name cb]
  (am/with-checkpoint name
    (cb)))

(defn ^:export with-data [data cb]
  (am/with-data (js->clj data)
    (cb)))

(defn ^:export trace [msg & [data]]
  (a/trace msg (js->clj data)))

(defn ^:export debug [msg & [data]]
  (a/debug msg (js->clj data)))

(defn ^:export info [msg & [data]]
  (a/info msg (js->clj data)))

(defn ^:export warn [msg & [data]]
  (a/warn msg (js->clj data)))

(defn ^:export error [msg & [data]]
  (a/error msg (js->clj data)))

(defn ^:export fatal [msg & [data]]
  (a/fatal msg (js->clj data)))
