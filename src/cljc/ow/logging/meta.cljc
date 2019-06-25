(ns ow.logging.meta
  #?(:cljs (:require-macros [ow.logging.core :as cm]))
  #?(:clj  (:require [ow.logging.core :as cm]
                     [ow.logging.core :as c]
                     [ow.logging.log :as l])
     :cljs (:require [ow.logging.core :as c]
                     [ow.logging.log :as l]))
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

(defmacro with-detached-logging-info [obj & body]
  `(cm/with-historical-logging-info (detach ~obj)
     ~@body))
