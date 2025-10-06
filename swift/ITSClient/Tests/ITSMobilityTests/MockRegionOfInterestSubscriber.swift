//
// Software Name: SWRMobility
// SPDX-FileCopyrightText: Copyright (c) Orange SA
//
// This software is confidential and proprietary information of Orange SA.
// You shall not disclose such Confidential Information and shall not copy,
// use or distribute it in whole or in part without the prior written
// consent of Orange SA.
//
// Software description: SWRMobility is a V2X collision prevention solution.

import Foundation
@testable import ITSMobility
import Testing

actor MockRegionOfInterestSubscriber: RegionOfInterestSubscriber {
    private(set)var numberOfSubscriptionsCalled = 0
    private(set)var numberOfUnsubscriptionsCalled = 0

    func subscribe(topic: String) async -> Bool {
        numberOfSubscriptionsCalled += 1
        return true
    }

    func unsubscribe(topic: String) async -> Bool {
        numberOfUnsubscriptionsCalled += 1
        return true
    }
}
