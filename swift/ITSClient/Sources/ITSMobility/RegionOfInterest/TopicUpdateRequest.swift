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

struct TopicUpdateRequest {
        let subscriptions: [TopicUpdateItem]
        let unsubscriptions: [TopicUpdateItem]
}

struct TopicUpdateItem {
    let topic: String
    let quadkey: String
}
