(ns ow.logging.meta
  (:require [ow.logging.core :as c]
            [ow.logging.log :as l])
  #?(:clj  (:import [clojure.lang IObj])))

(defn attach
  "Attaches the current logging info as metadata to obj. Will not do anything if obj is
   not an instance of IObj, because metadata cannot be attached in this case."
  [obj]
  #?(:clj  (if (instance? IObj obj)
             (with-meta obj
               {::logging-info c/+logging-info+})
             (do (l/info "cannot attach log information to obj not implementing IObj" {:obj obj})
                 obj))
     :cljs {}))

(defn detach
  "Returns the attached logging info from obj."
  [obj]
  (some-> obj meta ::logging-info))

(defmacro with-logging-info
  "Sets the current logggin info to loginfo."
  [logging-info & body]
  `(binding [c/+logging-info+ (c/merge-logging-info ~logging-info c/+logging-info+)]
     ~@body))
