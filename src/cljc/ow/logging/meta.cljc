(ns ow.logging.meta
  (:require [ow.logging.core :as c]
            [ow.logging.log :as l])
  #?(:clj  (:import [clojure.lang IObj])))

(defn attach [obj]
  #?(:clj  (if (instance? IObj obj)
             (with-meta obj
               {::logging-info (c/current-logging-info)})
             (do (l/info "cannot attach log information to obj not implementing IObj" {:obj obj})
                 obj))
     :cljs (with-meta obj
             {::logging-info (c/current-logging-info)})))

(defn detach [obj]
  (some-> obj meta ::logging-info))
