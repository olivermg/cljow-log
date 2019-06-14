(ns ow.logging.meta
  (:require [ow.logging :as l])
  (:import [clojure.lang IObj]))

(defn attach
  "Attaches the current trace info map as metadata to data. Will not do anything if data is
   not an instance of IObj, because metadata cannot be attached in this case."
  [data]
  (if (instance? IObj data)
    (with-meta data
      {::callinfo l/+callinfo+})
    (do (l/info attach "cannot attach log information to data not implementing IObj" {:data data})
        data)))

(defn detach
  "Returns the attached trace info map from data."
  [data]
  (some-> data meta ::callinfo))

(defmacro with-detached-loginfo
  "Sets the current trace info map to loginfo."
  [loginfo & body]
  `(binding [l/+callinfo+ (merge-loginfo ~loginfo l/+callinfo+)]
     ~@body))
