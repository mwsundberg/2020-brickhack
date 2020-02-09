(ns brickhack.ribbons
  (:require [brickhack.common :as c]
    	    [quil.core :as q]
            [quil.middleware :as middleware]))

(def body (.-body js/document))
(def w (.-clientWidth body))
(def h (.-clientHeight body))

                                        ; This-sketch custom code
(def palette (rand-nth c/palettes))
(defn trail
  [id]
  (c/particle-trail id (q/random w) (q/random h) (rand-nth (:colors palette))))

; settings constants
(def noise-zoom 0.002)
(def display-field false)

(defn noise-field-radian
  "Get a position dependent radian"
  [x y]  
  (* 2 Math/PI (c/noise-field x y noise-zoom)))

(defn render-field-vector
  "Debugging radian noise fields"
  [x y]
  (let [r (noise-field-radian x y)]
    (q/stroke [0 0 0])
    (apply q/line x y (c/coords-with-radian x y r 5))
    (q/ellipse x y 2 2)))

(defn render-field
  "Render a whole field of vectors"
  [width height]
  (doseq [x (range 0 width 10)]
    (doseq [y (range 0 height 10)]
      (render-field-vector x y))))

                                        ; Start of the sketch codes

(defn sketch-setup []
                                        ; Set color mode to HSB (HSV) instead of default RGB
  (q/color-mode :hsb 360 100 100 1.0)
  (q/no-stroke)
  (apply q/background (:background palette))
                                        ; Create 2000 particles at the start
  ; (render-field w h)
  (q/no-stroke)
  (sort-by (fn [trail]
             (:y (first (:points trail))))
           (map trail (range 0 200))))

(defn sketch-update [trails]
  (->> trails
       (map (fn [trail]
              (let [points       (:points trail)
                    velocity     (c/point-sub (first points) (second points))
                    theta        (noise-field-radian (:x (first points)) (:y (first points)))
                    new-velocity {:x (c/average (:x velocity) (Math/cos theta))
                                  :y (c/average (:y velocity) (Math/sin theta))}]
                (assoc trail :points
                  (cons (c/point-add (first points) new-velocity)
                        points)))))))

(defn sketch-draw [trails]
 	(apply q/background (:background palette))
  (doseq [trail trails]
    (apply q/fill (:color trail))
    (q/begin-shape)
    (q/vertex (:x (first (:points trail))) h)
    (doseq [point (:points trail)]
      (q/vertex (:x point) (:y point)))
    (q/vertex (:x (last (:points trail))) h)
    (q/end-shape :close))
  (when display-field (render-field w h) (q/no-loop)))

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
               (q/noise-seed 1243))))

(defonce sketch (create "sketch"))
