(ns ow.logging.api.alpha
  (:refer-clojure :rename {defn defn-clj
                           fn   fn-clj
                           let  let-clj})
  #?(:cljs (:require-macros [ow.logging.core :as cm]
                            [ow.logging.clojure :as cljm]
                            [ow.logging.log :as lm]
                            [ow.logging.meta :as mtm]))
  #?(:clj  (:require [ow.logging.core :as c]
                     [ow.logging.core :as cm]
                     [ow.logging.clojure :as clj]
                     [ow.logging.clojure :as cljm]
                     [ow.logging.log :as l]
                     [ow.logging.log :as lm]
                     [ow.logging.meta :as mt]
                     [ow.logging.meta :as mtm])
     :cljs (:require [ow.logging.core :as c]
                     [ow.logging.clojure :as clj]
                     [ow.logging.log :as l]
                     [ow.logging.meta :as mt])))

(defmacro with-checkpoint
  "Adds a logging checkpoint."
  [name & body]
  `(cm/with-checkpoint ~name ~@body))

(defmacro with-data
  "Adds user data into the current logging info, so that it will be available in subsequent log invocations."
  [data & body]
  `(cm/with-data ~data ~@body))

(defn-clj get-checkpoints
  "Returns the current logging checkpoints."
  []
  (c/get-checkpoints))

(defn-clj get-root-checkpoint
  "Returns the root/first/topmost logging checkpoint."
  []
  (c/get-root-checkpoint))

(defn-clj log-data
  "Returns current logging data augmented with msg and data."
  [level msg & [data]]
  (l/log-data level msg data))

(defn-clj log
  "Prints a log message based on the current logging info."
  [level msg & [data]]
  (l/log level msg data))

(defn-clj trace [msg & [data]]
  (l/trace msg data))

(defn-clj debug [msg & [data]]
  (l/debug msg data))

(defn-clj info [msg & [data]]
  (l/info msg data))

(defn-clj warn [msg & [data]]
  (l/warn msg data))

(defn-clj error [msg & [data]]
  (l/error msg data))

(defn-clj fatal [msg & [data]]
  (l/fatal msg data))

(defn-clj attach
  "Attaches the current logging info as metadata to obj. Will not do anything if obj is
   not an instance of IObj, because metadata cannot be attached in this case."
  [obj]
  (mt/attach obj))

(defn-clj detach
  "Returns the attached logging info from obj."
  [obj]
  (mt/detach obj))

(defmacro with-initialized-logging
  "Initializes logging and runs body within its scope."
  [& body]
  `(cm/with-initialized-logging ~@body))

(defmacro with-instance
  "Encapsulates a single logging instance (e.g. a request)."
  [& body]
  `(cm/with-instance ~@body))

(defmacro with-logging-info
  "Sets the current logging info to logging-info."
  [logging-info & body]
  `(cm/with-logging-info ~logging-info ~@body))

(defmacro with-historical-logging-info
  "Merges given logging-info with current logging-info. Assumes that given logging-info is 'older'
  than the current one and thus prepends the former's checkpoints to the latter's."
  [logging-info & body]
  `(cm/with-historical-logging-info ~logging-info ~@body))

(defmacro fn
  "Same as clojure.core/fn, but also adds a logging checkpoint upon invocation of the fn."
  [name [& args] & body]
  `(cljm/fn ~name [~@args] ~@body))

(defmacro defn
  "Same as clojure.core/defn, but also adds a logging checkpoint upon invocation of the defn."
  [name [& args] & body]
  `(cljm/defn ~name [~@args] ~@body))

(defmacro let
  "Like clojure.core/let, but also sets the current logging info to
  potential logging infos that might be attached to the given values."
  [[& bindings] & body]
  `(cljm/let [~@bindings] ~@body))
