import numpy as np
from numpy import array

class ClassifierWrapper(object):
    
    def __init__(self, cls, mode, min_cases_for_training = 30):
        self.cls = cls
        
        self.min_cases_for_training = min_cases_for_training
        self.hardcoded_prediction = None
        self.mode = mode

        
    def fit(self, X, y):
        # if there are too few training instances, use the mean
        if X.shape[0] < self.min_cases_for_training:
            self.hardcoded_prediction = np.mean(y)

        # if all the training instances are of the same class, use this class as prediction
        elif len(set(y)) < 2:
            self.hardcoded_prediction = int(y[0])

        else:
            self.cls.fit(X, y)
        return self
    
    
    def predict_proba(self, X, y=None):

        if self.hardcoded_prediction is not None:
            return array([self.hardcoded_prediction] * X.shape[0])
                        
        elif self.mode == "regr":
            preds = self.cls.predict(X)
            return preds

        elif self.mode == "class":
            preds_pos_label_idx = np.where(self.cls.classes_ == 1)[0][0]
            preds = self.cls.predict_proba(X)[:,preds_pos_label_idx]
            return preds

        else:
            print("Unrecognized training mode")
            return None
        
    
    def fit_predict(self, X, y):
        
        self.fit(X, y)
        return self.predict_proba(X)