(ns ow.logging.api.alpha
  (:refer-clojure :rename {defn defn-clj
                           fn   fn-clj
                           let  let-clj})
  (:require [ow.logging :as l]
            [ow.logging.clojure :as lc]
            [ow.logging.meta :as lm]))

(defmacro with-trace [& args]
  `(l/with-trace ~@args))
(defmacro with-trace-data [& args]
  `(l/with-trace-data ~@args))
(def get-trace l/get-trace)
(def get-trace-root l/get-trace-root)
(def log-data l/log-data)
(def log l/log)
(def trace l/trace)
(def debug l/debug)
(def info l/info)
(def warn l/warn)
(def error l/error)
(def fatal l/fatal)

(def attach lm/attach)
(def detach lm/detach)
(defmacro with-loginfo [& args]
  `(lm/with-loginfo ~@args))

(defmacro fn [& args]
  `(lc/fn ~@args))
(defmacro defn [& args]
  `(lc/defn ~@args))
(defmacro let [& args]
  `(lc/let ~@args))
