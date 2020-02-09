(ns brickhack.intersections
  (:require [brickhack.common :as c]
    	    [quil.core :as q]
            [quil.middleware :as middleware]))

(def body (.-body js/document))
(def w (.-clientWidth body))
(def h (.-clientHeight body))

                                        ; This-sketch custom code
(def palette (rand-nth c/palettes))
(defn particle [id] (c/particle id w h palette))

(def noise-zoom 0.002)

(defn noise-field-radian
  "Get a position dependent radian"
  [x y]  
  (* 4 Math/PI (c/noise-field x y noise-zoom)))
(defn noise-field-color
  [x y i]
  (nth (:colors palette) (c/normalize-to (c/noise-field x y noise-zoom (/ i 1000)) (count (:colors palette)))))

                                        ; Start of the sketch codes

(defn sketch-setup []
                                        ; Set color mode to HSB (HSV) instead of default RGB
  (q/color-mode :hsb 360 100 100 1.0)
  (q/no-stroke)
  (apply q/background (:background palette))
                                        ; Create 2000 particles at the start
  ; (render-field w h)
  (q/no-stroke)
  (map particle (range 0 2000)))

(defn sketch-update [particles]
  (->> particles
       (map (fn [p]
         (assoc p
                :x (c/add-with-rollover (:x p) (:vx p) w)
                :y (c/add-with-rollover (:y p) (:vy p) h)
                :length (+ 1 (:length p))
                :color (noise-field-color (:x p) (:y p) (:id p))
                :direction (noise-field-radian (:x p) (:y p))
                :vx (c/average (:dx p) (Math/cos (:direction p)))
                :vy (c/average (:dy p) (Math/sin (:direction p)))))
       particles)
       (filter (fn [p]
                 (>= 10000 (:length p))))))

(defn sketch-draw [particles]
 ; (apply q/background (:background palette))
  (doseq [p particles]
    (apply q/fill (:color p))
    (q/ellipse (:x p) (:y p) (:size p) (:size p))))

(defn create [canvas]
  (q/sketch
   :host canvas
   :size [w h]
   :draw #'sketch-draw
   :setup #'sketch-setup
   :update #'sketch-update
   :middleware [middleware/fun-mode]
   :settings (fn []
               (q/random-seed 432)
               (q/noise-seed 432))))

(defonce sketch (create "sketch"))
