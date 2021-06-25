//
//  BankedWrapper.swift
//  BankedReactNativeExample
//
//  Created by Kristina Borisova on 22/06/2021.
//

import Foundation
import UIKit
import Banked

@objc class BankedCheckoutWrapper: NSObject {
     
  @objc static let shared: BankedCheckoutWrapper = BankedCheckoutWrapper()
  
  @objc func setUp(apiKey: String) {
    BankedCheckout.shared.setUp(apiKey)
  }
  
  @objc func presentCheckout(viewController: UIViewController ,paymentId: String, continueURL: String) {
    BankedCheckout.shared.presentCheckout(viewController , paymentId: paymentId, action: .pay, continueURL: continueURL) { response in
      switch response {
      case .success:
        print("success")
      case .failure(let error):
        print("error \(error)")
      }
    }
  }
  
  @objc func handlePayment(url: URL) {
    
    BankedCheckout.shared.handlePaymentWithURL(url, action: .pay) { response in
      switch response {
      case .success:
        print("success")
      case .failure(let error):
        print("error \(error)")
      }
    }
  }
}

