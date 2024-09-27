use crate::client::configuration::configuration_error::ConfigurationError;
use crate::client::configuration::get_mandatory_field;
use ini::Properties;

pub(crate) const GEO_SECTION: &str = "geo";

/// Configuration of the geo_routing feature
///
/// Contains the information to build [GeoTopic][1]s
///
/// Example
/// ```ini
/// [geo]
/// prefix=myProject
/// suffix=my_domain
/// ```
///
/// [1]: crate::transport::mqtt::geo_topic::GeoTopic
pub struct GeoConfiguration {
    pub prefix: String,
    pub suffix: String,
}

impl TryFrom<&Properties> for GeoConfiguration {
    type Error = ConfigurationError;

    fn try_from(properties: &Properties) -> Result<Self, Self::Error> {
        Ok(Self {
            prefix: get_mandatory_field::<String>("prefix", ("geo", properties))?,
            suffix: get_mandatory_field::<String>("suffix", ("geo", properties))?,
        })
    }
}
