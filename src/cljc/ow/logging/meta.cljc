(ns ow.logging.meta
  (:require [ow.logging.core :as c]
            [ow.logging.log :as l])
  #?(:clj  (:import [clojure.lang IObj])))

(defn attach
  "Attaches the current trace info map as metadata to obj. Will not do anything if obj is
   not an instance of IObj, because metadata cannot be attached in this case."
  [obj]
  #?(:clj  (if (instance? IObj obj)
             (with-meta obj
               {::callinfo c/+callinfo+})
             (do (l/info attach "cannot attach log information to obj not implementing IObj" {:obj obj})
                 obj))
     :cljs {}))

(defn detach
  "Returns the attached trace info map from obj."
  [obj]
  (some-> obj meta ::callinfo))

(defmacro with-detached-loginfo
  "Sets the current trace info map to loginfo."
  [loginfo & body]
  `(binding [c/+callinfo+ (merge-loginfo ~loginfo c/+callinfo+)]
     ~@body))
